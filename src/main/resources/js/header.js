  const header = document.getElementById('main-header');

  header.innerHTML = `

    <div id="logo" class="logo-tech" onclick="window.location.href='../home/home.html'">
      <span class="null-part">NULL</span>
      <span class="archive-part">ARCHIVE</span>
      <img src="../../assets/img/logo-icon.png" alt="Icon" class="logo-icon">
    </div>

    <nav class="user-nav">
        <div id="user-actions" class="nav-group" style="display:none;">
          <span class="nav-text">@<span>Anonymous</span></span>
          <span class="nav-text">| Deseja compartilhar?</span>
          <a href="../login/login.html" class="nav-link">Entrar</a>
          <a href="../cadastro/cadastro.html" class="nav-btn">Criar Conta</a>
        </div>
          
        <div id="account-actions" class="nav-group" style="display:none;">
          <span class="nav-text">@<span id="display-name"></span></span>
          <div id="admin-actions" style="display:none;">
              <a href="../painel-admin/painel-admin.html" class="link-admin">Painel Admin</a>
          </div>
          <a href="../novo-livro/novo-livro.html" class="btn-action">Novo Livro</a>
          

          <button id="logout" class="logout-btn">Sair</button>
        </div>
    </nav>
  `;

  document.getElementById("logout").addEventListener("click", async function (e) {
      e.preventDefault(); // 🚫 impede o submit padrão
      
      const res = await fetch("http://127.0.0.1:8081/editor/logout", {
        method: "POST",
        credentials: "include",
        
      });
      
      if (res.ok) {
        window.location.href = "../home/home.html";
      } else {
        const text = await res.text();
        alert("Erro: " + text);
      }
  });
    

  async function loadData() {
    const res = await fetch("http://127.0.0.1:8081/editor/data", {
      credentials: "include"
    });
    
    if (!res.ok) {
      showUser();
      return;
    }
    
    const data = await res.json();
    
    
    if (data.authenticated === true) {
      if(data.role === "ADMIN"){
        showAdmin(data);
      }
      else{
        showEditor(data);
      }
    } else {
      showUser();
    }
  }

  function showUser() {
    document.getElementById("user-actions").style.display = "block";
    document.getElementById("account-actions").style.display = "none";
    document.getElementById("admin-actions").style.display = "none";
  }

 function showEditor(data) {
    document.getElementById("user-actions").style.display = "none";
    document.getElementById("account-actions").style.display = "flex"; // Ativa o Flex
    document.getElementById("admin-actions").style.display = "none";
    document.getElementById("display-name").textContent = data.username;
  }

function showAdmin(data) {
    document.getElementById("user-actions").style.display = "none";
    document.getElementById("account-actions").style.display = "flex"; // Ativa o Flex
    document.getElementById("admin-actions").style.display = "flex";  // Aparece na linha
    document.getElementById("display-name").textContent = data.username;
  }

loadData();
