# E-commerce API Documentation

## Table of Contents
1. [Authentication](#authentication)
2. [Users](#users)
3. [Products](#products)
4. [Orders](#orders)
5. [Wishlist](#wishlist)
6. [Reviews](#reviews)

## Authentication

### Login
- **Endpoint**: `/api/auth/login`
- **Method**: `POST`
- **Description**: Authenticate user and get JWT token

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (Success - 200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "roles": ["ROLE_USER"]
}
```

**Response (Error - 401 Unauthorized)**:
```json
{
  "message": "Bad credentials"
}
```

## Users

### Get User Profile
- **Endpoint**: `/api/user/profile`
- **Method**: `GET`
- **Authentication**: Required (JWT)

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890"
}
```

### Update Profile
- **Endpoint**: `/api/user/profile`
- **Method**: `PUT`
- **Authentication**: Required (JWT)

**Request Body**:
```json
{
  "name": "John Doe Updated",
  "phone": "+1234567891"
}
```

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "name": "John Doe Updated",
  "email": "john@example.com",
  "phone": "+1234567891"
}
```

## Products

### Get All Products
- **Endpoint**: `/api/products`
- **Method**: `GET`
- **Authentication**: Not required

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "name": "Product 1",
    "description": "Description 1",
    "price": 99.99,
    "category": "ELECTRONICS"
  },
  {
    "id": 2,
    "name": "Product 2",
    "description": "Description 2",
    "price": 199.99,
    "category": "CLOTHING"
  }
]
```

### Get Product by ID
- **Endpoint**: `/api/products/{id}`
- **Method**: `GET`
- **Authentication**: Not required

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "name": "Product 1",
  "description": "Description 1",
  "price": 99.99,
  "category": "ELECTRONICS",
  "stockQuantity": 50,
  "createdAt": "2023-01-01T00:00:00Z"
}
```

## Orders

### Place Order
- **Endpoint**: `/api/orders`
- **Method**: `POST`
- **Authentication**: Required (JWT)

**Request Body**:
```json
{
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "orderItems": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

**Response (Success - 201 Created)**:
```json
{
  "orderId": 1,
  "orderDate": "2023-10-09T12:00:00Z",
  "totalAmount": 199.98,
  "status": "PENDING"
}
```

## Wishlist

### Add to Wishlist
- **Endpoint**: `/api/wishlist/{productId}`
- **Method**: `POST`
- **Authentication**: Required (JWT)

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "userId": 1,
  "productId": 1,
  "addedAt": "2023-10-09T12:00:00Z"
}
```

### Get Wishlist
- **Endpoint**: `/api/wishlist`
- **Method**: `GET`
- **Authentication**: Required (JWT)

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "product": {
      "id": 1,
      "name": "Product 1",
      "price": 99.99
    },
    "addedAt": "2023-10-09T12:00:00Z"
  }
]
```

## Reviews

### Create Review
- **Endpoint**: `/api/reviews`
- **Method**: `POST`
- **Authentication**: Required (JWT)

**Request Body**:
```json
{
  "productId": 1,
  "rating": 5,
  "comment": "Great product!"
}
```

**Response (Success - 201 Created)**:
```json
{
  "id": 1,
  "productId": 1,
  "userId": 1,
  "userName": "John Doe",
  "rating": 5,
  "comment": "Great product!",
  "createdAt": "2023-10-09T12:00:00Z"
}
```

# Testing the APIs

## Using cURL

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

### Get Products
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Content-Type: application/json"
```

### Place Order (with JWT)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"shippingAddress":{"street":"123 Main St","city":"New York","state":"NY","zipCode":"10001","country":"USA"},"orderItems":[{"productId":1,"quantity":2}]}'
```

## Using Postman

1. Import the following collection into Postman:
   - Go to File > Import
   - Import the following JSON:

```json
{
  "info": {
    "name": "E-commerce API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Login",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"user@example.com\",\n  \"password\": \"password123\"\n}"
        },
        "url": "http://localhost:8080/api/auth/login"
      }
    },
    {
      "name": "Get Products",
      "request": {
        "method": "GET",
        "header": [],
        "url": "http://localhost:8080/api/products"
      }
    },
    {
      "name": "Place Order",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"shippingAddress\": {\n    \"street\": \"123 Main St\",\n    \"city\": \"New York\",\n    \"state\": \"NY\",\n    \"zipCode\": \"10001\",\n    \"country\": \"USA\"\n  },\n  \"orderItems\": [\n    {\n      \"productId\": 1,\n      \"quantity\": 2\n    }\n  ]\n}"
        },
        "url": "http://localhost:8080/api/orders"
      }
    }
  ]
}
```

## Test Cases

### Authentication Tests
1. **Valid Login**
   - **Test**: Login with valid credentials
   - **Expected**: 200 OK with JWT token

2. **Invalid Login**
   - **Test**: Login with invalid credentials
   - **Expected**: 401 Unauthorized

### Product Tests
1. **Get All Products**
   - **Test**: Fetch all products
   - **Expected**: 200 OK with array of products

2. **Get Non-existent Product**
   - **Test**: Fetch product with ID 9999
   - **Expected**: 404 Not Found

### Order Tests
1. **Place Order - Valid**
   - **Test**: Place order with valid data
   - **Expected**: 201 Created with order details

2. **Place Order - No Auth**
   - **Test**: Place order without JWT token
   - **Expected**: 401 Unauthorized

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "timestamp": "2023-10-09T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "must be a well-formed email address"
    }
  ]
}
```

#### 401 Unauthorized
```json
{
  "timestamp": "2023-10-09T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/orders"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2023-10-09T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/admin/users"
}
```

#### 404 Not Found
```json
{
  "timestamp": "2023-10-09T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 9999",
  "path": "/api/products/9999"
}
```

#### 500 Internal Server Error
```json
{
  "timestamp": "2023-10-09T12:00:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/orders"
}
```

## Rate Limiting
- All endpoints are rate limited to 1000 requests per hour per IP address
- Authentication endpoints have a lower limit of 100 requests per hour per IP
- Exceeding the rate limit will result in a 429 Too Many Requests response

## Versioning
- Current API version: v1
- Version is included in the URL path (e.g., `/api/v1/products`)
- Default version is used when no version is specified

## Authentication
- JWT (JSON Web Token) authentication is used
- Include the token in the `Authorization` header: `Bearer <token>`
- Tokens expire after 24 hours
- Refresh tokens can be used to obtain new access tokens

## Pagination
Endpoints that return lists of items support pagination using the following query parameters:
- `page`: Page number (0-based, default: 0)
- `size`: Number of items per page (default: 10)
- `sort`: Sort criteria in the format: `property,direction` (e.g., `name,asc`)

Example:
```
GET /api/products?page=0&size=20&sort=price,desc
```

## Filtering
Some endpoints support filtering using query parameters. The available filters depend on the endpoint.

Example:
```
GET /api/products?category=ELECTRONICS&minPrice=100&maxPrice=1000
```

## Field Selection
Use the `fields` parameter to specify which fields to include in the response. Separate multiple fields with commas.

Example:
```
GET /api/products/1?fields=id,name,price
```

## Response Format
All responses are in JSON format and include the following structure:
- For successful requests: `200 OK` with the requested data
- For created resources: `201 Created` with the created resource
- For no content: `204 No Content`
- For errors: Appropriate status code with error details

## Date and Time
All dates and times are in UTC and formatted according to ISO 8601 (e.g., `2023-10-09T12:00:00Z`)

## Data Validation
- Request bodies are validated automatically
- Validation errors include details about which fields failed validation
- Common validations include:
  - Required fields
  - Data types
  - String length
  - Email format
  - Numeric ranges

## Cross-Origin Resource Sharing (CORS)
- CORS is enabled for all origins in development
- In production, only trusted domains are allowed
- Preflight requests are supported

## Compression
- Responses are compressed using gzip when supported by the client
- The following content types are compressed:
  - `application/json`
  - `text/*`
  - `application/xml`
  - `application/javascript`

## Caching
- Responses include appropriate cache headers
- Public resources can be cached by clients and CDNs
- Private resources include `Cache-Control: private`
- ETags are supported for conditional requests

## Security Headers
The API includes the following security headers by default:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Content-Security-Policy: default-src 'self'`
- `Strict-Transport-Security: max-age=31536000 ; includeSubDomains`

## WebSocket Support
Real-time updates are available via WebSocket at `/ws`. The following events are supported:
- Order status updates
- Price changes
- Stock updates

## Webhook Support
Webhooks can be configured to receive notifications for various events. See the Webhooks section for details.

## Monitoring
- Health check endpoint: `/actuator/health`
- Metrics endpoint: `/actuator/metrics`
- API documentation: `/swagger-ui.html`

## Deprecation Policy
- Endpoints will be marked as deprecated at least one version before removal
- Deprecated endpoints will continue to work for at least 6 months
- Notices about deprecated endpoints will be included in the response headers

## Support
For support, please contact:
- Email: support@example.com
- Slack: #api-support
- Documentation: https://docs.example.com/api

## Changelog
### v1.0.0 (2023-10-01)
- Initial release
- Basic CRUD operations for products, orders, and users
- JWT authentication
- Role-based access control

## License
This API is licensed under the [MIT License](https://opensource.org/licenses/MIT).

---
*Documentation generated on October 9, 2023*
