import { kpis, bars, linePoints, areaPoints, unidades, heat, clientes } from './mock';
// import { http } from './http';

export const dashboardService = {
  // Mock: devolve tudo que o dashboard precisa em um único payload.
  async getVisaoGeral() {
    return { kpis, bars, linePoints, areaPoints, unidades, heat, clientes };

    // Backend Java:
    // return http.get('/dashboard');
  },
};
