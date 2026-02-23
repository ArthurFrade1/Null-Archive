async function atualizaBooks(){
      const grid = document.getElementById('livros-grid');

      const res = await fetch(`http://127.0.0.1:80/search/books?admin`, {
        credentials: "include"
      });
      
      const data = await res.json();

      grid.innerHTML = '';

      data.forEach(book => {
        const card = document.createElement('div');
        card.className = 'book-card';
    
        const coverUrl = book.storage_path_image 
            ? `http://127.0.0.1:80/user/image/${book.storage_path_image}` 
            : 'placeholder.png';
    
            // Gerar o HTML das tags dinamicamente
          // Pegamos apenas as 3 primeiras (slice) caso o back mande mais por erro
    
          let tagsArray = [];
    
          if (book.tags) {
              if (Array.isArray(book.tags)) {
                  // Se já for um array (veio tratado do Java)
                  tagsArray = book.tags;
              } else if (typeof book.tags === 'string') {
                  // Se veio como string "terror,fantasia", transforma em array
                  tagsArray = book.tags.split(',');
              }
          }
    
          const tagsHtml = tagsArray
              .slice(0, 3) 
              .map(tag => `<span class="tag-pill">${tag.trim()}</span>`)
              .join('');
    
              
        card.innerHTML = `
            <div class="card-cover">
                <img src="${coverUrl}" alt="Capa de ${book.title}">
            </div>
            <div class="card-info">
                <h3 class="book-title">${book.title}</h3>
                <span class="book-author">${book.author_name || 'Autor desconhecido'}</span>
                
                <div class="card-tags">
                    ${tagsHtml}
                </div>
            </div>
        `;
    
        card.addEventListener('click', () => {
            window.location.href = `/livro?id=${book.id}&admin`;
        });
    
        grid.appendChild(card);
      });
    }
    atualizaBooks();