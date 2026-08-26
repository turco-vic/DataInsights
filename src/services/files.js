import { arquivos, uploadResumo } from './mock';
// import { http } from './http';

export const filesService = {
  // Mock: lista de planilhas enviadas.
  async listar() {
    return arquivos;

    // Backend Java:
    // return http.get('/planilhas');
  },

  // Mock: processa um upload e devolve o resumo do tratamento.
  async enviar(/* arquivo */) {
    return uploadResumo;

    // Backend Java (multipart):
    // const form = new FormData(); form.append('file', arquivo);
    // return http.post('/planilhas', form, { headers: {} });
  },
};
