document.getElementById("loginForm").addEventListener("submit", async function (e) {
    e.preventDefault(); // 🚫 impede o submit padrão

    const form = e.target;

    const data = {
        email: form.email.value,
        password: form.password.value
    };

    const response = await fetch("http://127.0.0.1:80/editor/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        credentials: "include",   // ← AQUI
        body: JSON.stringify(data)
    });

    if (response.ok) {
        window.location.href = "/home";
    } else {
        const text = await response.text();
        alert("Erro: " + text);
    }
});