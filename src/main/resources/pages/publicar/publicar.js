
    const form = document.getElementById("bookForm");
    const statusEl = document.getElementById("status");
    const resetBtn = document.getElementById("resetBtn");
    const container = document.getElementById('container-tags');
    let contaTags = 0;
    let tagsSelecionadas = []; // Array para guardar os IDs

    async function loadTags() {
        const res = await fetch("http://127.0.0.1:80/editor/tags", {
            credentials: "include"
        });

        if (!res.ok) {
            return;
        }


        const data = await res.json();

        // Limpa o container antes de carregar (caso a função seja chamada mais de uma vez)
        //container.innerHTML = '';

        // --- O PULO DO GATO ESTÁ AQUI ---
        // Percorremos cada item (tag) dentro do array 'data'
        data.forEach(tag => {
            // 2. Cria o botão dinamicamente para CADA tag
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.innerText = tag.nome;
            btn.classList.add('btn-tag');

            // 3. Lógica do Clique
            btn.addEventListener('click', () => {
                toggleTag(btn, tag.id);
            });

            // Adiciona o botão criado ao container na tela
            container.appendChild(btn);
        });


    }
    loadTags()

    function toggleTag(elemento, id) {
        if (tagsSelecionadas.includes(id)) {
            // Se já está no array, remove (desmarcar)
            tagsSelecionadas = tagsSelecionadas.filter(item => item !== id);
            elemento.classList.remove('selecionado');
            contaTags--;
        } else {
            if (contaTags < 3) {
                // Se não está, adiciona (marcar)
                tagsSelecionadas.push(id);
                elemento.classList.add('selecionado');
                contaTags++;
            }
        }

    }


    resetBtn.addEventListener("click", () => {
        form.reset();
        statusEl.textContent = "";
    });

    function setStatus(msg) {
        if (statusEl) {
            statusEl.textContent = msg;
        } else {
            console.log("Status update: " + msg);
        }
    }

    function updateFileName(inputId, spanId) {
        const input = document.getElementById(inputId);
        const span = document.getElementById(spanId);
        const wrapper = input.parentElement;

        if (input.files && input.files.length > 0) {
            // Pega o nome do arquivo
            let fileName = input.files[0].name;

            // Se o nome for muito grande, corta para não quebrar o layout
            if (fileName.length > 25) {
                fileName = fileName.substring(0, 22) + "...";
            }

            span.innerText = fileName.toUpperCase();
            wrapper.classList.add('file-selected');
        } else {
            span.innerText = inputId === 'book_file' ? "SELECIONAR DOCUMENTO" : "CARREGAR ARTE DA CAPA";
            wrapper.classList.remove('file-selected');
        }
    }


    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const fileInput = document.getElementById("book_file");
        const fileBook = fileInput.files[0];
        if (!fileBook) {
            setStatus("Selecione um arquivo.");
            return;
        }
        const imageInput = document.getElementById("image_book");
        const fileImage = imageInput.files[0];
        if (!fileImage) {
            setStatus("Selecione uma imagem.");
            return;
        }

        const eraValue = document.getElementById('era').value === "AC";

        const payload = {
            title: document.getElementById("title").value,
            author_name: document.getElementById("author_name").value || null,
            description: document.getElementById("description").value || null,
            language_code: document.getElementById("language_code").value || null,
            published_year: document.getElementById("published_year").value ? Number(document.getElementById("published_year").value) : null,
            license: document.getElementById("license").value,
            source_url: document.getElementById("source_url").value || null,

            // REATIVADO E CORRIGIDO:
            file_kind: document.getElementById("file_kind").value,

            era: eraValue,
            tags: tagsSelecionadas
        };

        const fd = new FormData();
        fd.append("meta", new Blob([JSON.stringify(payload)], { type: "application/json" }));
        fd.append("file", fileBook);
        fd.append("image", fileImage);

        setStatus("Enviando...");

        try {
            const res = await fetch("http://127.0.0.1:80/editor/book", {
                method: "POST",
                credentials: "include",
                body: fd
            });

            if (res.ok) {
                // MOSTRAR MODAL DE SUCESSO
                document.getElementById("successModal").style.display = "flex";
            } else {
                const errorText = await res.text();
                alert("Falha na sincronização: " + errorText);
            }
        } catch (err) {
            alert("Erro de rede. Verifique o servidor.");
        }
    });