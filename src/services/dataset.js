import { colunas, qualidade, insights, bars, linePoints, areaPoints } from './mock';
// import { http } from './http';

export const datasetService = {
  // Mock: relatório completo de um dataset (identificado por id no backend real).
  async obter(/* id */) {
    return { colunas, qualidade, insights, bars, linePoints, areaPoints };

    // Backend Java:
    // return http.get(`/datasets/${id}`);
  },
};
