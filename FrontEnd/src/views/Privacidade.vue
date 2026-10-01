<script setup>
import PaginaLegal from '../components/PaginaLegal.vue';

const contato = import.meta.env.VITE_CONTATO_EMAIL;
</script>

<template>
  <PaginaLegal titulo="Política de privacidade" atualizado-em="30/09/2026">
    <p>
      Esta política explica quais dados pessoais a DataInsights trata, para quê e como você pode exercer os seus
      direitos previstos na Lei Geral de Proteção de Dados (Lei nº 13.709/2018).
    </p>

    <h2>1. Dados que tratamos</h2>
    <ul>
      <li><strong>Conta:</strong> nome, e-mail, empresa, cargo, papel, preferências, data do último acesso e da última troca de senha. A senha é guardada apenas como hash (bcrypt), nunca em texto.</li>
      <li><strong>Planilhas de clientes:</strong> código, nome, e-mail, segmento, nível, faturamento anual, serviços contratados, data de contratação, cidade, UF e consultor responsável.</li>
      <li><strong>Registros de uso (telemetria):</strong> logins, tentativas de login recusadas, importações, exclusões e trocas de senha, com data e hora.</li>
    </ul>

    <h2>2. Para que usamos</h2>
    <ul>
      <li>Autenticar o acesso e aplicar as permissões de cada papel.</li>
      <li>Importar clientes, gerar o dashboard, o relatório do dataset e insights por cliente.</li>
      <li>Enviar o link de recuperação de senha e, se você ativar, o resumo semanal por e-mail.</li>
      <li>Investigar problemas e acessos indevidos a partir da telemetria.</li>
    </ul>

    <h2>3. Armazenamento no navegador</h2>
    <p>
      Guardamos no seu navegador apenas o token da sessão e dados básicos do seu perfil. Com “Manter conectado”, eles
      ficam por até 7 dias; sem essa opção, somem ao fechar o navegador. Não usamos cookies de rastreamento nem de publicidade.
    </p>

    <h2>4. Compartilhamento</h2>
    <p>
      Os dados ficam visíveis apenas para as contas da empresa contratante, conforme o papel de cada uma. Não vendemos
      nem compartilhamos dados com terceiros, exceto provedores de infraestrutura (hospedagem e envio de e-mail) necessários para o serviço.
    </p>

    <h2>5. Retenção</h2>
    <p>
      Os dados permanecem enquanto a empresa usar a plataforma. Administradores e analistas podem excluir clientes,
      contratos, insights e o histórico de planilhas; administradores podem excluir contas.
    </p>

    <h2>6. Seus direitos</h2>
    <p>
      Você pode pedir acesso, correção, anonimização ou exclusão dos seus dados. Dados da conta podem ser corrigidos
      em Configurações; para os demais pedidos, fale com o administrador da sua empresa<span v-if="contato">
      ou escreva para <a :href="`mailto:${contato}`" class="font-semibold text-ocean hover:text-navy">{{ contato }}</a></span>.
    </p>
  </PaginaLegal>
</template>
