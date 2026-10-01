package com.cti.provedor.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.dto.ClienteDTO;
import com.cti.provedor.dto.ClienteImportacaoDTO;
import com.cti.provedor.dto.ImportacaoResultadoDTO;
import com.cti.provedor.exception.ConflitoException;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.model.Cliente;
import com.cti.provedor.model.Consultor;
import com.cti.provedor.model.Contrato;
import com.cti.provedor.model.Servico;
import com.cti.provedor.repository.ClienteRepository;
import com.cti.provedor.repository.ConsultorRepository;
import com.cti.provedor.repository.ContratoRepository;
import com.cti.provedor.repository.InsightRepository;
import com.cti.provedor.repository.ServicoRepository;
import com.cti.provedor.repository.TelemetriaRepository;

import jakarta.validation.Validator;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ConsultorRepository consultorRepository;
    private final ServicoRepository servicoRepository;
    private final ContratoRepository contratoRepository;
    private final InsightRepository insightRepository;
    private final TelemetriaRepository telemetriaRepository;
    private final EventoService eventos;
    private final Validator validator;

    public ClienteService(ClienteRepository clienteRepository, ConsultorRepository consultorRepository,
            ServicoRepository servicoRepository, ContratoRepository contratoRepository,
            InsightRepository insightRepository, TelemetriaRepository telemetriaRepository, EventoService eventos,
            Validator validator) {
        this.clienteRepository = clienteRepository;
        this.consultorRepository = consultorRepository;
        this.servicoRepository = servicoRepository;
        this.contratoRepository = contratoRepository;
        this.insightRepository = insightRepository;
        this.telemetriaRepository = telemetriaRepository;
        this.eventos = eventos;
        this.validator = validator;
    }

    /** Filtros opcionais: busca (nome, código ou e-mail), segmento, nível, UF e consultor. */
    public List<Cliente> listar(String busca, String segmento, String nivel, String uf, Long consultorId) {
        Specification<Cliente> filtro = (raiz, consulta, cb) -> cb.conjunction();
        if (busca != null && !busca.isBlank()) {
            String termo = "%" + busca.trim().toLowerCase() + "%";
            filtro = filtro.and((raiz, consulta, cb) -> cb.or(
                    cb.like(cb.lower(raiz.get("nome")), termo),
                    cb.like(cb.lower(raiz.get("codigo")), termo),
                    cb.like(cb.lower(raiz.get("email")), termo)));
        }
        if (segmento != null && !segmento.isBlank()) {
            filtro = filtro.and((raiz, consulta, cb) -> cb.equal(raiz.get("segmento"), segmento));
        }
        if (nivel != null && !nivel.isBlank()) {
            filtro = filtro.and((raiz, consulta, cb) -> cb.equal(raiz.get("nivel"), nivel));
        }
        if (uf != null && !uf.isBlank()) {
            filtro = filtro.and((raiz, consulta, cb) -> cb.equal(raiz.get("uf"), uf));
        }
        if (consultorId != null) {
            filtro = filtro.and((raiz, consulta, cb) -> cb.equal(raiz.get("consultor").get("id"), consultorId));
        }
        return clienteRepository.findAll(filtro, Sort.by("nome"));
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    @Transactional
    public Cliente criar(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        preencher(cliente, dto);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Long id, ClienteDTO dto) {
        Cliente cliente = buscarPorId(id);
        preencher(cliente, dto);
        return clienteRepository.save(cliente);
    }

    /** Remove o cliente junto com os contratos, insights e telemetrias dele. */
    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        insightRepository.deleteByContratoClienteId(id);
        insightRepository.deleteByClienteId(id);
        telemetriaRepository.deleteByClienteId(id);
        contratoRepository.deleteByClienteId(id);
        clienteRepository.delete(cliente);
        eventos.registrar(EventoService.CLIENTE_EXCLUIDO, "Cliente excluído: " + cliente.getCodigo() + " · " + cliente.getNome());
    }

    private void preencher(Cliente cliente, ClienteDTO dto) {
        String codigo = dto.getCodigo().trim();
        String email = dto.getEmail().trim().toLowerCase();
        Long id = cliente.getId();

        boolean codigoEmUso = id == null
                ? clienteRepository.existsByCodigo(codigo)
                : clienteRepository.existsByCodigoAndIdNot(codigo, id);
        if (codigoEmUso) {
            throw new ConflitoException("Código de cliente já cadastrado");
        }
        boolean emailEmUso = id == null
                ? clienteRepository.existsByEmailIgnoreCase(email)
                : clienteRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
        if (emailEmUso) {
            throw new ConflitoException("E-mail já pertence a outro cliente");
        }

        cliente.setCodigo(codigo);
        cliente.setNome(dto.getNome().trim());
        cliente.setEmail(email);
        cliente.setSegmento(dto.getSegmento());
        cliente.setNivel(dto.getNivel());
        cliente.setFaturamentoAnual(dto.getFaturamentoAnual());
        cliente.setDataContratacao(dto.getDataContratacao());
        cliente.setCidade(dto.getCidade());
        cliente.setUf(dto.getUf());
        cliente.setConsultor(dto.getConsultorId() == null ? null
                : consultorRepository.findById(dto.getConsultorId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Consultor não encontrado")));
    }

    @Transactional
    public ImportacaoResultadoDTO importar(List<ClienteImportacaoDTO> dados) {
        ImportacaoResultadoDTO resultado = new ImportacaoResultadoDTO(dados.size());
        Set<String> codigosNoLote = new HashSet<>();
        Set<String> emailsNoLote = new HashSet<>();

        for (ClienteImportacaoDTO dto : dados) {
            String motivo = validar(dto, codigosNoLote, emailsNoLote);
            if (motivo != null) {
                resultado.rejeitar(dto.getCodigoCliente(), motivo);
                continue;
            }

            Optional<Cliente> existente = clienteRepository.findByCodigo(dto.getCodigoCliente());
            Cliente cliente = existente.orElseGet(Cliente::new);
            preencher(cliente, dto);
            clienteRepository.save(cliente);
            vincularServicos(cliente, dto);

            if (existente.isPresent()) {
                resultado.somarAtualizado();
            } else {
                resultado.somarCriado();
            }
        }

        return resultado;
    }

    private String validar(ClienteImportacaoDTO dto, Set<String> codigosNoLote, Set<String> emailsNoLote) {
        String erros = validator.validate(dto).stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        if (!erros.isEmpty()) {
            return erros;
        }
        if (!codigosNoLote.add(dto.getCodigoCliente())) {
            return "codigoCliente repetido no envio";
        }
        if (!emailsNoLote.add(dto.getEmail().toLowerCase())) {
            return "email repetido no envio";
        }
        if (clienteRepository.existsByEmailIgnoreCaseAndCodigoNot(dto.getEmail(), dto.getCodigoCliente())) {
            return "email já pertence a outro cliente";
        }
        return null;
    }

    private void preencher(Cliente cliente, ClienteImportacaoDTO dto) {
        cliente.setCodigo(dto.getCodigoCliente());
        cliente.setNome(dto.getNomeCliente().trim());
        cliente.setEmail(dto.getEmail().trim().toLowerCase());
        cliente.setSegmento(dto.getSegmento());
        cliente.setNivel(dto.getNivelCliente());
        cliente.setFaturamentoAnual(dto.getFaturamentoAnual());
        cliente.setDataContratacao(dto.getDataContratacao());
        cliente.setCidade(dto.getCidade());
        cliente.setUf(dto.getUf());
        cliente.setConsultor(buscarOuCriarConsultor(dto.getConsultor().trim()));
    }

    private Consultor buscarOuCriarConsultor(String nome) {
        return consultorRepository.findFirstByNomeIgnoreCase(nome).orElseGet(() -> {
            Consultor novo = new Consultor();
            novo.setNome(nome);
            return consultorRepository.save(novo);
        });
    }

    private void vincularServicos(Cliente cliente, ClienteImportacaoDTO dto) {
        for (String nome : dto.getServicosContratados()) {
            Servico servico = buscarOuCriarServico(nome.trim());
            if (contratoRepository.existsByClienteAndServico(cliente, servico)) {
                continue;
            }
            Contrato contrato = new Contrato();
            contrato.setCliente(cliente);
            contrato.setServico(servico);
            contrato.setDataInicio(dto.getDataContratacao());
            contrato.setAtivo(true);
            contratoRepository.save(contrato);
        }
    }

    private Servico buscarOuCriarServico(String nome) {
        return servicoRepository.findFirstByNomeIgnoreCase(nome).orElseGet(() -> {
            Servico novo = new Servico();
            novo.setNome(nome);
            return servicoRepository.save(novo);
        });
    }
}
