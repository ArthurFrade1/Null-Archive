let book;
async function loadBookDetails() {
    const urlParams = new URLSearchParams(window.location.search);
    const bookId = urlParams.get('id');

    if (!bookId) {
        document.body.innerHTML = "<h1>ID NÃO LOCALIZADO NO SISTEMA</h1>";
        return;
    }

    try {
        const res = await fetch(`http://127.0.0.1:8081/book/info?id=${bookId}`, { credentials: "include" });
        book = await res.json();
        

        // 1. Preenchendo os campos básicos
        document.getElementById('book-title').innerText = book.title;
        document.getElementById('book-author').innerText = book.author_name;
        document.getElementById('info-lang').innerText = book.language_code;
        document.getElementById('info-year').innerText = book.published_year + (book.is_bc ? " a.C" : " d.C");
        document.getElementById('info-license').innerText = book.license;
        document.getElementById('info-filename').innerText = book.original_filename;
        document.getElementById('info-user').innerText = `@${book.username || 'user_unknown'}`;
        document.getElementById('info-size').innerText = (book.size_bytes / (1024 * 1024)).toFixed(2);
        document.getElementById('info-date').innerText = new Date(book.created_at).toLocaleDateString();
        document.getElementById('book-description').innerText = book.description || 'Nenhuma descrição técnica disponível.';
        
        // Imagem
        document.getElementById('book-cover').src = book.storage_path_image 
            ? `http://127.0.0.1:8081/user/image/${book.storage_path_image}` 
            : 'placeholder.png';

        // URL de Fonte
        const sourceLink = document.getElementById('book-source');
        if (book.source_url) {
            sourceLink.href = book.source_url;
            sourceLink.innerText = book.source_url;
        }

        // 2. Gerando Tags
        const tagsContainer = document.getElementById('book-tags');
        let tagsArray = typeof book.tags === 'string' ? book.tags.split(',') : (book.tags || []);
        tagsContainer.innerHTML = tagsArray.map(t => `<span class="tag-pill">${t.trim()}</span>`).join('');

        // 3. Configurando Botões de Ação
        document.getElementById('btn-download').onclick = () => baixarArquivo(book.storage_path_file, book.original_filename);
        document.getElementById('btn-view').onclick = () => lerArquivo(book.storage_path_file);

        // 4. Verificação de Admin
        if (urlParams.has('admin')) {
            const adminRes = await fetch('http://127.0.0.1:8081/editor/data', { credentials: "include" });
            const account = await adminRes.json();
            
            if (account.role === "ADMIN") {
                document.getElementById('admin-decision').style.display = 'block';
                
                // Configura as funções de clique dos botões admin
                document.getElementById('btn-approve').onclick = () => moderarLivro(bookId, 'approve');
                document.getElementById('btn-reject').onclick = () => moderarLivro(bookId, 'desapprove');
            }
        }


        document.getElementById('book-content').style.display = 'grid';

    } catch (err) {
        console.error("Erro ao carregar:", err);
    }
}

// Função para download
function baixarArquivo(storage_path_file, original_filename) {
    const url = `http://localhost:8081/book/file/${storage_path_file}?action=download&name=${encodeURIComponent(original_filename)}`;
    const a = document.createElement('a');
    a.href = url;
    a.download = original_filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
}

async function lerArquivo(storage_path_file) {

    const response = await fetch('http://127.0.0.1:8081/user/reading', { //Atualiza a tabela de reading_progress nesse usuáro no banco
        method: 'POST',
        credentials: "include",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ book_id: book.id })
    });

    window.open(`http://localhost:8081/book/file/${storage_path_file}?action=view`, '_blank');
    

}

// Função de Moderação (Resolvendo o problema do await)
async function moderarLivro(id, acao) {
    const confirmacao = confirm(`Deseja realmente executar a ação: ${acao}?`);
    if (!confirmacao) return;

    const response = await fetch('http://127.0.0.1:8081/admin/approve', {
        method: 'POST',
        credentials: "include",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id: id, action: acao })
    });

    if (response.ok) {
        alert("Ação concluída!");
        window.location.href = "gerenciarLivros.html";
    } else {
        alert("Erro ao processar ação.");
    }
}

loadBookDetails();