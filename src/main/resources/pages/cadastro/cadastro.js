document.getElementById("registerForm").addEventListener("submit", async function (e) {
    e.preventDefault(); // 🚫 impede o submit padrão

    const form = e.target;

    const data = {
        username: form.username.value,
        password: form.password.value,
        email: form.email.value
    };

    const response = await fetch("http://127.0.0.1:80/editor/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    });

    if (response.ok) {
        alert("Usuário criado com sucesso!");
        window.location.href = "../login/login.html";
    } else {
        const text = await response.text();
        alert("Erro: " + text);
    }
});