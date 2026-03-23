import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 10,
  duration: "30s",
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<1200"]
  }
};

const baseUrl = __ENV.BASE_URL || "http://localhost:8080";

function randomEmail() {
  return `user_${Date.now()}_${Math.floor(Math.random() * 100000)}@example.com`;
}

export default function () {
  const email = randomEmail();
  const password = "password123";

  const registerResponse = http.post(`${baseUrl}/api/users/register`, JSON.stringify({
    name: "Load Test User",
    email,
    password
  }), {
    headers: { "Content-Type": "application/json" }
  });

  check(registerResponse, {
    "register succeeds": (r) => r.status === 201
  });

  const token = registerResponse.json("token");
  const authHeaders = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`
  };

  const productResponse = http.post(`${baseUrl}/api/products`, JSON.stringify({
    name: "k6 Product",
    description: "Load testing product",
    price: 19.99,
    stock: 100,
    category: "LoadTest"
  }), {
    headers: authHeaders
  });

  check(productResponse, {
    "product created": (r) => r.status === 201
  });

  const productId = productResponse.json("id");

  const cartResponse = http.post(`${baseUrl}/api/cart/items`, JSON.stringify({
    productId,
    productName: "k6 Product",
    price: 19.99,
    quantity: 1
  }), {
    headers: authHeaders
  });

  check(cartResponse, {
    "cart updated": (r) => r.status === 200
  });

  const orderResponse = http.post(`${baseUrl}/api/orders`, JSON.stringify({
    items: [
      {
        productId,
        quantity: 1,
        price: 19.99
      }
    ]
  }), {
    headers: authHeaders
  });

  check(orderResponse, {
    "order created": (r) => r.status === 201
  });

  sleep(1);
}
