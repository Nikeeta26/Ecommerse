package com.ecommerce.repository;

import com.ecommerce.model.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final EntityManager entityManager;

    public OrderRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }



    @Override
    public List<Order> findBySubscriptionIdAndType(Long subscriptionId, Order.OrderType type, Sort sort) {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(Order.class);
        var order = cq.from(Order.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(order.get("subscriptionId"), subscriptionId));
        predicates.add(cb.equal(order.get("type"), type));

        cq.where(predicates.toArray(new Predicate[0]));

        // Apply sorting
        if (sort != null) {
            List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
            for (Sort.Order o : sort) {
                if (o.isAscending()) {
                    orders.add(cb.asc(order.get(o.getProperty())));
                } else {
                    orders.add(cb.desc(order.get(o.getProperty())));
                }
            }
            cq.orderBy(orders);
        }

        TypedQuery<Order> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    @Override
    public Page<Order> findBySubscriptionIdAndType(Long subscriptionId, Order.OrderType type, Pageable pageable) {
        var cb = entityManager.getCriteriaBuilder();
        
        // Create count query
        var countQuery = cb.createQuery(Long.class);
        var orderCount = countQuery.from(Order.class);
        
        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.equal(orderCount.get("subscriptionId"), subscriptionId));
        countPredicates.add(cb.equal(orderCount.get("type"), type));
        
        countQuery.select(cb.count(orderCount)).where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        // Create main query
        var cq = cb.createQuery(Order.class);
        var order = cq.from(Order.class);
        
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(order.get("subscriptionId"), subscriptionId));
        predicates.add(cb.equal(order.get("type"), type));
        
        cq.where(predicates.toArray(new Predicate[0]));
        
        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
            for (Sort.Order o : pageable.getSort()) {
                if (o.isAscending()) {
                    orders.add(cb.asc(order.get(o.getProperty())));
                } else {
                    orders.add(cb.desc(order.get(o.getProperty())));
                }
            }
            cq.orderBy(orders);
        }
        
        TypedQuery<Order> query = entityManager.createQuery(cq);
        
        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<Order> result = query.getResultList();
        
        return new PageImpl<>(result, pageable, total);
    }
}
