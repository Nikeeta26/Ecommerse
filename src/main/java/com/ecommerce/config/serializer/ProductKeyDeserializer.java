package com.ecommerce.config.serializer;

import com.ecommerce.model.Product;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class ProductKeyDeserializer extends JsonDeserializer<Product> {
    @Override
    public Product deserialize(JsonParser p, DeserializationContext ctxt) 
        throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isNumber()) {
            // If the key is just an ID
            Product product = new Product();
            product.setId(node.asLong());
            return product;
        } else if (node.isObject()) {
            // If the key is a full product object
            return p.getCodec().treeToValue(node, Product.class);
        }
        return null;
    }
}
