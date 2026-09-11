import { arquivos, uploadResumo } from './mock';
// import { http } from './http';

export const filesService = {
  // Mock: lista de planilhas enviadas.
  async listar() {
    return arquivos;

    // Backend Java:
    // return http.get('/planilhas');
  },

  // ATENÇÃO: não usado pela tela de upload. Desde que o uploadStore passou a ler
  // e tratar a planilha no navegador, quem manda é src/stores/uploadStore.js.
  // Esta função continua devolvendo mock — é apenas o ponto de entrada reservado
  // para o Spring Boot. Ao ligar o backend, troque o corpo pelo POST real e
  // chame-a de uma action enviarParaBackend() no store; não a use como está.
  async enviar(/* arquivo */) {
    return uploadResumo;

    // Backend Java (multipart):
    // const form = new FormData(); form.append('file', arquivo);
    // return http.post('/planilhas', form, { headers: {} });
  },
};
