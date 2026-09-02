import { usuarios } from './mock';
// import { http } from './http';

export const usersService = {
  // Mock: pessoas com acesso à plataforma.
  async listar() {
    return usuarios;

    // Backend Java:
    // return http.get('/usuarios');
  },
};
