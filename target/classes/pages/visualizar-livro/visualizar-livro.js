let book;

async function loadBookDetails() {
    const urlParams = new URLSearchParams(window.location.search);
    const bookId = urlParams.get('id');

    if (!bookId) {
        document.body.innerHTML = "<h1 style='color:white; text-align:center; margin-top:100px;'>ACCESS_DENIED: ID_NOT_FOUND</h1>";
        return;
    }

    try {
        const res = await fetch(`http://127.0.0.1:8081/book/info?id=${bookId}`, { credentials: "include" });
        book = await res.json();

        // Populando dados
        document.getElementById('book-title').innerText = book.title;
        document.getElementById('book-author').innerText = book.author_name;
        document.getElementById('info-lang').innerText = book.language_code;
        document.getElementById('info-year').innerText = book.published_year + (book.is_bc ? " a.C" : " d.C");
        document.getElementById('info-license').innerText = book.license;
        document.getElementById('info-kind').innerText = book.file_kind || 'UNKNOWN';
        document.getElementById('info-user').innerText = `@${book.username || 'archive_bot'}`;
        document.getElementById('info-size').innerText = (book.size_bytes / (1024 * 1024)).toFixed(2);
        document.getElementById('info-date').innerText = new Date(book.created_at).toLocaleDateString();
        document.getElementById('book-description').innerText = book.description || 'No record log description.';
        document.getElementById('book-source').innerText = book.source_url;

        document.getElementById('book-source').href = book.source_url;

        // Cover
        document.getElementById('book-cover').src = `http://127.0.0.1:8081/user/image/${book.storage_path_image}`;

        // Tags
        const tagsContainer = document.getElementById('book-tags');
        let tagsArray = typeof book.tags === 'string' ? book.tags.split(',') : (book.tags || []);
        tagsContainer.innerHTML = tagsArray.map(t => `<span class="tag-pill">${t.trim()}</span>`).join('');

        // Ações
        document.getElementById('btn-download').onclick = () => baixarArquivo(book.storage_path_file, book.original_filename);
        document.getElementById('btn-view').onclick = () => lerArquivo(book.storage_path_file);

        // Admin Check
        if (urlParams.has('admin')) {
            const adminRes = await fetch('http://127.0.0.1:8081/editor/data', { credentials: "include" });
            const account = await adminRes.json();
            if (account.role === "ADMIN") {
                document.getElementById('admin-decision').style.display = 'block';
                document.getElementById('btn-approve').onclick = () => moderarLivro(bookId, 'approve');
                document.getElementById('btn-reject').onclick = () => moderarLivro(bookId, 'desapprove');
            }
        }

        document.getElementById('book-content').style.display = 'grid';

    } catch (err) {
        console.error("Critical System Error:", err);
    }
}

function baixarArquivo(storage_path_file, original_filename) {
    const url = `http://localhost:8081/book/file/${storage_path_file}?action=download&name=${encodeURIComponent(original_filename)}`;
    const a = document.createElement('a');
    a.href = url;
    a.download = original_filename;
    a.click();
}

async function lerArquivo(storage_path_file) {
    await fetch('http://127.0.0.1:8081/user/reading', {
        method: 'POST', credentials: "include",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ book_id: book.id })
    });
    window.open(`http://localhost:8081/book/file/${storage_path_file}?action=view`, '_blank');
}

async function moderarLivro(id, acao) {
    if (!confirm(`Deseja executar a ação: ${acao}?`)) return;
    const response = await fetch('http://127.0.0.1:8081/admin/approve', {
        method: 'POST', credentials: "include",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id: id, action: acao })
    });
    if (response.ok) { window.location.href = "../painel-admin/painel-admin.html"; }
}

loadBookDetails();