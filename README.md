# Multithreaded Proxy Web Server with Rate Limiting, Caching, IP Whitelisting/Blacklisting, and Geo-Location Based Blocking

## Project Description

This project is a **Multithreaded Proxy Web Server** built using **Spring Boot**, designed to handle multiple requests efficiently, offering several advanced features such as **Rate Limiting**, **Caching**, **IP Whitelisting/Blacklisting**, and **Geo-Location Based Blocking**. It acts as a proxy between a client and a target host, enabling controlled access to resources while optimizing performance and security.

---

## Features

### 1. **Rate Limiting**
- Restricts the number of requests a user can make in a given time frame, preventing abuse or overuse of system resources.
- Implemented using a custom **Rate Limiter**.
- Example: A user is allowed 10 requests per minute. After exceeding the limit, further requests are rejected until the time window resets.

### 2. **Cache Storing (LRU Cache)**
- Uses an **LRU (Least Recently Used)** cache to store responses and serve repeated requests faster.
- If a request is cached, the server returns the response from the cache, reducing the need to query the actual target host.
- The cache has a fixed size and removes the least recently used entries when full.

### 3. **IP Whitelisting and Blacklisting**
- Provides fine-grained control over which IPs can access the service.
- **Whitelist**: Only specific IP addresses are allowed to make requests.
- **Blacklist**: Requests from specific IPs are blocked.
- Configurable via in-memory lists or external configuration files.

### 4. **Geo-Location Based Blocking**
- Integrates with external geo-location services to restrict access based on the geographical location of the request's IP address.
- Uses `https://api.ipify.org` to fetch the client's public IP and `http://ip-api.com` to get geo-location information.
- Requests originating from certain blocked countries are rejected.

---

## Tech Stack

- **Java**: The main programming language used for building the application.
- **Spring Boot**: A Java-based framework for building microservices and web applications. The project is structured using Spring Boot for ease of development and deployment.
- **Spring WebFlux**: Used for handling non-blocking reactive streams and handling IP-based request filtering.
- **Micrometer**: For monitoring and exposing application metrics such as request latency and error rates.
- **SLF4J**: Logging framework used for tracking events and errors.
- **LRU Cache**: A custom Least Recently Used (LRU) cache implementation to optimize repeated requests.
- **Rate Limiting**: Custom implementation to manage and throttle user requests.
- **ip-api.com**: External API used to fetch geo-location information.
- **api.ipify.org**: External API used to fetch the public IP of the client.

---

## Microservices/External Services

1. **Proxy Service**: Acts as a middle layer between clients and the actual host. Fetches resources and caches responses.
2. **GeoLocation Service**: Fetches geo-location details based on the client's public IP to determine if requests should be blocked.
    - Fetches public IP using `https://api.ipify.org`.
    - Fetches country and other geo-location information from `http://ip-api.com`.
3. **Rate Limiting Service**: Controls the rate of requests per user to prevent abuse.
4. **Cache Management Service**: Stores responses in an LRU cache to enhance performance.

---

## Setup and Running the Project

1. **Prerequisites**:
    - JDK 11 or higher
    - Maven

2. **Clone the Repository**:
   ```bash
   git clone https://github.com/RuntimeTerror-404/advance_proxy_server.git
   cd proxy-server
