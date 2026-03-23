const gatewayBaseUrl = "http://localhost:8080";

const state = {
    token: null,
    products: [],
    cart: null
};

const authOutput = document.getElementById("authOutput");
const productOutput = document.getElementById("productOutput");
const cartOutput = document.getElementById("cartOutput");
const ordersOutput = document.getElementById("ordersOutput");
const notificationsOutput = document.getElementById("notificationsOutput");
const productsContainer = document.getElementById("products");

function renderSection(target, title, data) {
    target.textContent = `[${new Date().toLocaleTimeString()}] ${title}\n${JSON.stringify(data, null, 2)}`;
}

function logError(target, message) {
    renderSection(target, "Error", { message });
}

function setValue(id, value) {
    document.getElementById(id).value = value;
}

function getAuthHeaders() {
    return state.token
        ? {
            "Authorization": `Bearer ${state.token}`
        }
        : {};
}

async function request(path, options = {}) {
    let response;
    try {
        response = await fetch(`${gatewayBaseUrl}${path}`, {
            headers: {
                "Content-Type": "application/json",
                ...getAuthHeaders(),
                ...(options.headers || {})
            },
            ...options
        });
    } catch (error) {
        throw new Error("Could not reach the API Gateway. Make sure docker compose is fully up and CORS is enabled.");
    }

    const text = await response.text();
    let body = null;
    if (text) {
        try {
            body = JSON.parse(text);
        } catch (error) {
            body = { raw: text };
        }
    }

    if (!response.ok) {
        throw new Error(body?.message || body?.error || `Request failed with status ${response.status}`);
    }

    return body?.data ?? body;
}

function readValue(id) {
    return document.getElementById(id).value;
}

function renderProducts() {
    productsContainer.innerHTML = "";

    if (!state.products.length) {
        productsContainer.innerHTML = "<p class='hint'>No products loaded yet.</p>";
        return;
    }

    state.products.forEach((product) => {
        const card = document.createElement("article");
        card.className = "card";
        card.innerHTML = `
            <span class="tag">${product.category}</span>
            <h3>${product.name}</h3>
            <p>${product.description}</p>
            <p><strong>$${product.price}</strong> · Stock: ${product.stock}</p>
            <button data-product-id="${product.id}">Add To Cart</button>
        `;
        card.querySelector("button").addEventListener("click", () => addToCart(product));
        productsContainer.appendChild(card);
    });
}

async function register() {
    const body = {
        name: readValue("name"),
        email: readValue("email"),
        password: readValue("password")
    };
    const data = await request("/api/users/register", {
        method: "POST",
        body: JSON.stringify(body)
    });
    state.token = data.token;
    renderSection(authOutput, "Registered user", {
        tokenPreview: `${data.token.slice(0, 24)}...`,
        user: data.user
    });
}

async function login() {
    const body = {
        email: readValue("email"),
        password: readValue("password")
    };
    const data = await request("/api/users/login", {
        method: "POST",
        body: JSON.stringify(body)
    });
    state.token = data.token;
    renderSection(authOutput, "Logged in", {
        tokenPreview: `${data.token.slice(0, 24)}...`,
        user: data.user
    });
}

async function createProduct() {
    const body = {
        name: readValue("productName"),
        description: readValue("productDescription"),
        price: Number(readValue("productPrice")),
        stock: Number(readValue("productStock")),
        category: readValue("productCategory")
    };
    const data = await request("/api/products", {
        method: "POST",
        body: JSON.stringify(body)
    });
    renderSection(productOutput, "Created product", data);
    await loadProducts();
}

async function loadProducts() {
    const data = await request("/api/products");
    state.products = data;
    renderProducts();
    renderSection(productOutput, "Loaded products", data);
}

async function addToCart(product) {
    const data = await request("/api/cart/items", {
        method: "POST",
        body: JSON.stringify({
            productId: product.id,
            productName: product.name,
            price: product.price,
            quantity: 1
        })
    });
    state.cart = data;
    renderSection(cartOutput, "Added to cart", data);
}

async function loadCart() {
    const data = await request("/api/cart");
    state.cart = data;
    renderSection(cartOutput, "Loaded cart", data);
}

async function createOrder() {
    if (!state.cart?.items?.length) {
        throw new Error("Load the cart and add at least one item before creating an order.");
    }

    const data = await request("/api/orders", {
        method: "POST",
        body: JSON.stringify({
            items: state.cart.items.map((item) => ({
                productId: item.productId,
                quantity: item.quantity,
                price: item.price
            }))
        })
    });
    renderSection(ordersOutput, "Created order", data);
}

async function loadOrders() {
    const data = await request("/api/orders");
    renderSection(ordersOutput, "Loaded orders", data);
}

async function loadNotifications() {
    const data = await request("/api/notifications");
    renderSection(notificationsOutput, "Loaded notifications", data);
}

async function run(action, target) {
    try {
        await action();
    } catch (error) {
        logError(target, error.message);
    }
}

document.getElementById("registerBtn").addEventListener("click", () => run(register, authOutput));
document.getElementById("loginBtn").addEventListener("click", () => run(login, authOutput));
document.getElementById("resetBtn").addEventListener("click", resetSession);
function resetSession() {
    state.token = null;
    state.products = [];
    state.cart = null;
    productsContainer.innerHTML = "";
    setValue("name", "");
    setValue("email", "");
    setValue("password", "");
    setValue("productName", "");
    setValue("productPrice", "");
    setValue("productStock", "");
    setValue("productCategory", "");
    setValue("productDescription", "");
    renderSection(authOutput, "Ready", { message: "Register or log in to begin." });
    renderSection(productOutput, "Ready", { message: "Create a product or load the catalog." });
    renderSection(cartOutput, "Ready", { message: "Cart details will appear here." });
    renderSection(ordersOutput, "Ready", { message: "Order results will appear here." });
    renderSection(notificationsOutput, "Ready", { message: "Notification results will appear here." });
}
document.getElementById("createProductBtn").addEventListener("click", () => run(createProduct, productOutput));
document.getElementById("loadProductsBtn").addEventListener("click", () => run(loadProducts, productOutput));
document.getElementById("refreshProductsBtn").addEventListener("click", () => run(loadProducts, productOutput));
document.getElementById("loadCartBtn").addEventListener("click", () => run(loadCart, cartOutput));
document.getElementById("createOrderBtn").addEventListener("click", () => run(createOrder, ordersOutput));
document.getElementById("loadOrdersBtn").addEventListener("click", () => run(loadOrders, ordersOutput));
document.getElementById("loadNotificationsBtn").addEventListener("click", () => run(loadNotifications, notificationsOutput));

renderSection(authOutput, "Ready", {
    message: "Register or log in to begin."
});
renderSection(productOutput, "Ready", {
    message: "Create a product or load the catalog."
});
renderSection(cartOutput, "Ready", {
    message: "Cart details will appear here."
});
renderSection(ordersOutput, "Ready", {
    message: "Order results will appear here."
});
renderSection(notificationsOutput, "Ready", {
    message: "Notification results will appear here."
});


