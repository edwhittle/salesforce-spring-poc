# Salesforce POC - Spring Boot Application with Lucene Search

## Overview
This Spring Boot application provides fast search capabilities for a large product dataset (400K+ products) using both traditional database queries and Apache Lucene full-text search indexing.

## Features
- **Product Management**: Complete CRUD operations for products
- **CSV Import**: Bulk import of products from CSV files
- **Database Search**: Traditional SQL-based search across multiple fields
- **Lucene Search**: High-performance full-text search with sub-second response times
- **Performance Comparison**: Built-in tools to compare search method performance

## Performance Benefits
For a dataset of 400K products:
- **Database Search**: 2-30 seconds depending on complexity
- **Lucene Search**: 5-50 milliseconds (50-600x faster)

## API Endpoints

### Product Management
- `GET /api/products/supplier/{supplier}` - Get products by single supplier ID
- `GET /api/products/suppliers/{suppliers}` - Get products by multiple supplier IDs (comma-separated)
- `GET /api/products/suppliers?suppliers={suppliers}` - Get products by multiple supplier IDs (query parameter)

### Search Endpoints
- `GET /api/search/lucene?query=supplierId&limit=50` - Fast supplier search (primary use case)
- `GET /api/search/supplier?supplierIds=12345,67890&limit=1000` - Optimized multi-supplier search
- `GET /api/search/lucene/field?field=fieldName&query=searchTerm&limit=50` - Search specific field
- `GET /api/search/database?query=searchTerm&limit=50` - Traditional database search
- `GET /api/search/compare?query=searchTerm` - Compare both search methods

### Index Management
- `POST /api/search/index/rebuild` - Rebuild Lucene index
- `GET /api/search/index/stats` - Get index statistics

## Searchable Fields
**Primary Index (Optimized for Performance):**
- **supplier**: Supplier ID (primary search field - fastest performance)

**Secondary Index:**
- **productId**: Product identifier
- **itemDescription**: Product description (searchable but not optimized)

**Stored Fields (retrievable but not indexed for search):**
- **supplierGroupId**: Supplier group identifier
- **smktsMerchCategory**: Merchandise category
- **liqMerchCategory**: Liquor merchandise category  
- **digitalBrandName**: Digital brand name
- **subBrandName**: Sub-brand name

## Quick Start

### 1. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### 2. Application will automatically:
- Import CSV data from `src/main/resources/data-all.csv`
- Create H2 database with optimized settings for 400K products
- Build Lucene search index
- Start web server on port 8080

### 3. Test Search Performance
```bash
# Fast supplier search (primary use case)
curl "http://localhost:8080/api/search/lucene?query=12345&limit=10"

# Optimized multi-supplier search
curl "http://localhost:8080/api/search/supplier?supplierIds=12345,67890,11111&limit=100"

# Search by single supplier ID (database)
curl "http://localhost:8080/api/products/supplier/12345"

# Search by multiple supplier IDs (path variable)
curl "http://localhost:8080/api/products/suppliers/12345,67890,11111"

# Search by multiple supplier IDs (query parameter)
curl "http://localhost:8080/api/products/suppliers?suppliers=12345,67890,11111"

# Compare performance
curl "http://localhost:8080/api/search/compare?query=wine"

# Database search (for comparison)
curl "http://localhost:8080/api/search/database?query=wine&limit=10"
```

## Configuration

### Application Properties
The application is optimized for large datasets with:
- **Persistent H2 Database**: Data survives application restarts
- **Connection Pooling**: 20 max connections, 5 minimum idle
- **Batch Processing**: 50 records per batch for optimal performance
- **Second-level Caching**: Enabled for frequently accessed data
- **JPA Optimizations**: Batch inserts, query optimization

### Memory Configuration
For 400K products, recommended JVM settings:
```bash
java -Xms2g -Xmx6g -jar salesforce-poc-0.0.1-SNAPSHOT.jar
```

## Search Examples

### Single Supplier Search
```bash
curl "http://localhost:8080/api/products/supplier/12345"
```

### Multiple Supplier Search
```bash
# Using path variable (recommended for short lists)
curl "http://localhost:8080/api/products/suppliers/12345,67890,11111"

# Using query parameter (better for long lists or special characters)
curl "http://localhost:8080/api/products/suppliers?suppliers=12345,67890,11111"

# With spaces (automatically trimmed)
curl "http://localhost:8080/api/products/suppliers?suppliers=12345, 67890, 11111"
```

### General Search (all fields)
```bash
# Search by supplier ID (fastest)
curl "http://localhost:8080/api/search/lucene?query=12345&limit=20"

# Multi-supplier search (optimized)
curl "http://localhost:8080/api/search/supplier?supplierIds=12345,67890,11111&limit=100"
```

### Specific Field Search
```bash
# Search by supplier (primary optimized field)
curl "http://localhost:8080/api/search/lucene/field?field=supplier&query=12345&limit=10"

# Search by product description
curl "http://localhost:8080/api/search/lucene/field?field=itemDescription&query=premium&limit=15"
```

### Complex Queries
Lucene supports advanced query syntax for supplier searches:
```bash
# Multiple suppliers (OR query)
curl "http://localhost:8080/api/search/lucene?query=12345%20OR%2067890"

# Specific supplier with wildcard
curl "http://localhost:8080/api/search/lucene?query=1234*"

# Range query for supplier IDs
curl "http://localhost:8080/api/search/lucene?query=supplier:[10000%20TO%2020000]"
```

## Performance Monitoring

### Index Statistics
```bash
curl "http://localhost:8080/api/search/index/stats"
```

### Performance Comparison
```bash
curl "http://localhost:8080/api/search/compare?query=searchTerm"
```

Response includes:
- Execution time for both methods
- Result counts
- Speed improvement factor

## Database Access
H2 Console available at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/productdb`
- Username: `sa`
- Password: (empty)

## File Structure
```
src/
├── main/
│   ├── java/com/example/salesforcepoc/
│   │   ├── controller/
│   │   │   ├── ProductController.java
│   │   │   └── SearchController.java
│   │   ├── entity/
│   │   │   └── Product.java
│   │   ├── repository/
│   │   │   └── ProductRepository.java
│   │   ├── service/
│   │   │   ├── ProductService.java
│   │   │   ├── CsvImportService.java
│   │   │   └── LuceneSearchService.java
│   │   └── SalesforcePocApplication.java
│   └── resources/
│       ├── application.properties
│       └── data-all.csv
└── lucene-index/          # Generated Lucene index files
```

## Troubleshooting

### Index Issues
If search returns no results, rebuild the index:
```bash
curl -X POST "http://localhost:8080/api/search/index/rebuild"
```

### Memory Issues
For large datasets, increase JVM heap size:
```bash
export MAVEN_OPTS="-Xms2g -Xmx6g"
mvn spring-boot:run
```

### Performance Issues
- Ensure index is built: Check `/api/search/index/stats`
- Monitor connection pool: Check application logs
- Use field-specific searches when possible for better performance

## Resource Requirements

### Minimum System Requirements
- **RAM**: 4GB (2GB for application, 2GB for OS)
- **Storage**: 5GB (3GB for database, 1GB for index, 1GB for application)
- **CPU**: 2 cores minimum, 4+ recommended for optimal performance

### Production Recommendations
- **RAM**: 8GB+ (6GB for application heap)
- **Storage**: SSD recommended for database and index files
- **CPU**: 4+ cores for concurrent search operations
