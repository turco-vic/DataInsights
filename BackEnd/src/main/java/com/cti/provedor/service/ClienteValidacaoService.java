package com.cti.provedor.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cti.provedor.dto.ClienteImportacaoDTO;
import com.cti.provedor.dto.InconsistenciaDTO;
import com.cti.provedor.exception.PlanilhaInconsistenteException;
import com.cti.provedor.exception.RegraNegocioException;
import com.cti.provedor.repository.ClienteRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Validação complementar do lote de clientes (aula de validação no backend).
 *
 * O DTO valida uma linha por vez. Aqui ficam as regras que precisam de lógica:
 * padronizar os textos, comparar as linhas entre si (código e e-mail repetidos) e
 * conferir o banco. A planilha só passa se estiver 100% consistente: qualquer
 * problema interrompe tudo e todas as inconsistências voltam para o front.
 */
@Service
public class ClienteValidacaoService {

    private static final Map<String, String> SEGMENTOS = Map.ofEntries(
            Map.entry("IND", "Indústria"),
            Map.entry("INDUSTRIA", "Indústria"),
            Map.entry("COM", "Comércio"),
            Map.entry("COMERCIO", "Comércio"),
            Map.entry("SERVICO", "Serviços"),
            Map.entry("SERVICOS", "Serviços"),
            Map.entry("SAUDE", "Saúde"),
            Map.entry("EDUCACAO", "Educação"),
            Map.entry("TECNOLOGIA", "Tecnologia"));

    /** Palavras que continuam minúsculas no meio de um nome ("Maria da Silva"). */
    private static final Set<String> CONECTORES = Set.of("de", "da", "do", "das", "dos", "e");

    private final Validator validator;
    private final ClienteRepository clienteRepository;

    public ClienteValidacaoService(Validator validator, ClienteRepository clienteRepository) {
        this.validator = validator;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Padroniza cada cliente e valida o lote inteiro.
     *
     * @return a lista padronizada, pronta para gravar ou seguir para Ciência de Dados
     * @throws PlanilhaInconsistenteException com todas as inconsistências, se houver alguma
     */
    public List<ClienteImportacaoDTO> validarEPreparar(List<ClienteImportacaoDTO> clientes) {
        if (clientes == null || clientes.isEmpty()) {
            throw new RegraNegocioException("A planilha não tem nenhum cliente para validar.");
        }

        List<ClienteImportacaoDTO> preparados = new ArrayList<>();
        List<InconsistenciaDTO> inconsistencias = new ArrayList<>();
        Map<String, Integer> codigos = new HashMap<>();
        Map<String, Integer> emails = new HashMap<>();

        for (int i = 0; i < clientes.size(); i++) {
            ClienteImportacaoDTO cliente = padronizar(clientes.get(i));
            // Sem número de linha (envio pelo Thunder Client, por exemplo), conta como planilha: cabeçalho na linha 1.
            int linha = cliente.getLinha() != null ? cliente.getLinha() : i + 2;
            cliente.setLinha(linha);
            preparados.add(cliente);

            // 1) Regras de uma linha: anotações do DTO.
            for (ConstraintViolation<ClienteImportacaoDTO> v : validator.validate(cliente)) {
                inconsistencias.add(new InconsistenciaDTO(linha, cliente.getCodigoCliente(),
                        coluna(v.getPropertyPath().toString()), v.getMessage()));
            }

            // 2) Regras do lote: o mesmo código ou e-mail não pode aparecer em duas linhas.
            repetido(codigos, cliente.getCodigoCliente(), linha, cliente, "codigo_cliente", "O código", inconsistencias);
            repetido(emails, cliente.getEmail(), linha, cliente, "email", "O e-mail", inconsistencias);

            // 3) Regra do banco: o e-mail não pode pertencer a outro cliente já cadastrado.
            if (cliente.getEmail() != null && cliente.getCodigoCliente() != null
                    && clienteRepository.existsByEmailIgnoreCaseAndCodigoNot(cliente.getEmail(), cliente.getCodigoCliente())) {
                inconsistencias.add(new InconsistenciaDTO(linha, cliente.getCodigoCliente(), "email",
                        "O e-mail " + cliente.getEmail() + " já pertence a outro cliente cadastrado"));
            }
        }

        if (!inconsistencias.isEmpty()) {
            inconsistencias.sort(Comparator.comparing(InconsistenciaDTO::linha).thenComparing(InconsistenciaDTO::campo));
            throw new PlanilhaInconsistenteException(inconsistencias);
        }
        return preparados;
    }

    /** Limpa e padroniza os textos de um cliente. Datas e números já chegam convertidos pelo JSON. */
    private ClienteImportacaoDTO padronizar(ClienteImportacaoDTO c) {
        ClienteImportacaoDTO p = new ClienteImportacaoDTO();
        p.setLinha(c.getLinha());
        p.setCodigoCliente(maiusculo(c.getCodigoCliente()));
        p.setNomeCliente(capitalizar(limpar(c.getNomeCliente())));
        p.setEmail(c.getEmail() == null ? null : limpar(c.getEmail()).toLowerCase());
        p.setConsultor(capitalizar(limpar(c.getConsultor())));
        p.setSegmento(padronizarSegmento(c.getSegmento()));
        p.setNivelCliente(maiusculo(c.getNivelCliente()));
        p.setFaturamentoAnual(c.getFaturamentoAnual());
        p.setServicosContratados(c.getServicosContratados() == null ? null
                : c.getServicosContratados().stream()
                        .map(ClienteValidacaoService::limpar)
                        .filter(s -> s != null && !s.isEmpty())
                        .distinct()
                        .toList());
        p.setDataContratacao(c.getDataContratacao());
        p.setCidade(vazioParaNulo(capitalizar(limpar(c.getCidade()))));
        p.setUf(vazioParaNulo(maiusculo(c.getUf())));
        return p;
    }

    /** "IND.", "industria" e "INDÚSTRIA" viram "Indústria". Valor desconhecido segue como veio e o DTO aponta. */
    static String padronizarSegmento(String segmento) {
        String limpo = limpar(segmento);
        if (limpo == null) {
            return null;
        }
        String chave = semAcento(limpo).toUpperCase().replace(".", "");
        return SEGMENTOS.getOrDefault(chave, limpo);
    }

    /** "ANA SOUZA" e "ana souza" viram "Ana Souza". Texto já com maiúsculas e minúsculas é mantido. */
    static String capitalizar(String texto) {
        if (texto == null || (!texto.equals(texto.toUpperCase()) && !texto.equals(texto.toLowerCase()))) {
            return texto;
        }
        String[] palavras = texto.toLowerCase().split(" ");
        for (int i = 0; i < palavras.length; i++) {
            String palavra = palavras[i];
            if (!palavra.isEmpty() && !(i > 0 && CONECTORES.contains(palavra))) {
                palavras[i] = palavra.substring(0, 1).toUpperCase() + palavra.substring(1);
            }
        }
        return String.join(" ", palavras);
    }

    /** Remove espaços nas pontas e espaços duplicados no meio. */
    static String limpar(String texto) {
        return texto == null ? null : texto.trim().replaceAll("\\s+", " ");
    }

    private static String maiusculo(String texto) {
        String limpo = limpar(texto);
        return limpo == null ? null : limpo.toUpperCase();
    }

    private static String vazioParaNulo(String texto) {
        return texto == null || texto.isEmpty() ? null : texto;
    }

    private static String semAcento(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static void repetido(Map<String, Integer> vistos, String valor, int linha, ClienteImportacaoDTO cliente,
            String campo, String rotulo, List<InconsistenciaDTO> inconsistencias) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        Integer primeira = vistos.putIfAbsent(valor.toLowerCase(), linha);
        if (primeira != null) {
            inconsistencias.add(new InconsistenciaDTO(linha, cliente.getCodigoCliente(), campo,
                    "%s %s já aparece na linha %d".formatted(rotulo, valor, primeira)));
        }
    }

    /** "faturamentoAnual" → "faturamento_anual"; "servicosContratados[0].<list element>" → "servicos_contratados". */
    private static String coluna(String caminho) {
        String campo = caminho.split("[\\[.]")[0];
        return Arrays.stream(campo.split("(?=[A-Z])"))
                .map(String::toLowerCase)
                .collect(Collectors.joining("_"));
    }
}
