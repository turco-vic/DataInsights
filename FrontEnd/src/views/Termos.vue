<script setup>
import { RouterLink } from 'vue-router';
import PaginaLegal from '../components/PaginaLegal.vue';
</script>

<template>
  <PaginaLegal titulo="Termos de uso" atualizado-em="30/09/2026">
    <p>
      Estes termos regulam o uso da plataforma DataInsights, que recebe planilhas de clientes, valida os dados
      e gera painéis, relatórios e insights para a equipe da empresa contratante.
    </p>

    <h2>1. Contas e acesso</h2>
    <ul>
      <li>Cada conta é pessoal. Não compartilhe a sua senha.</li>
      <li>A primeira conta criada é de administrador. As demais começam com acesso de leitura, e um administrador define o papel de cada pessoa (administrador, analista ou leitor).</li>
      <li>Consultores podem receber acesso de consulta à carteira de clientes.</li>
      <li>Administradores podem alterar papéis e remover contas a qualquer momento.</li>
    </ul>

    <h2>2. Uso permitido</h2>
    <ul>
      <li>Envie apenas planilhas que a sua empresa tem o direito de tratar e com dados necessários para a gestão da carteira.</li>
      <li>Não tente acessar dados ou funções além do que o seu papel permite, nem prejudicar o funcionamento da plataforma.</li>
    </ul>

    <h2>3. Dados enviados</h2>
    <p>
      Os dados das planilhas pertencem à empresa contratante. A plataforma os usa só para importar clientes,
      gerar indicadores, insights e o histórico de envios. O tratamento de dados pessoais está descrito na
      <RouterLink to="/privacidade" class="font-semibold text-ocean hover:text-navy">política de privacidade</RouterLink>.
    </p>

    <h2>4. Disponibilidade</h2>
    <p>
      Buscamos manter a plataforma disponível e os dados íntegros, mas podem ocorrer interrupções para manutenção.
      Guarde as planilhas originais: o sistema mantém os dados importados, não os arquivos enviados.
    </p>

    <h2>5. Alterações</h2>
    <p>Estes termos podem mudar. A data no topo indica a versão em vigor.</p>
  </PaginaLegal>
</template>
