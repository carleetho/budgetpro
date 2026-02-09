package com.budgetpro.application.compra.usecase;

import com.budgetpro.application.compra.dto.CompraDetalleResponse;
import com.budgetpro.application.compra.dto.RegistrarCompraCommand;
import com.budgetpro.application.compra.dto.RegistrarCompraResponse;
import com.budgetpro.application.compra.exception.BilleteraNoEncontradaException;
import com.budgetpro.application.compra.exception.ProyectoNoEncontradoException;
import com.budgetpro.application.compra.port.in.RegistrarCompraUseCase;
import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;
import com.budgetpro.domain.finanzas.consumo.port.out.ConsumoPartidaRepository;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.compra.model.CompraDetalleId;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.port.out.CompraRepository;
import com.budgetpro.domain.logistica.compra.service.ProcesarCompraService;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para registrar una compra.
 */
@Service
public class RegistrarCompraUseCaseImpl implements RegistrarCompraUseCase {

        private final CompraRepository compraRepository;
        private final ConsumoPartidaRepository consumoPartidaRepository;
        private final BilleteraRepository billeteraRepository;
        private final ProyectoRepository proyectoRepository;
        private final ProcesarCompraService procesarCompraService;

        public RegistrarCompraUseCaseImpl(CompraRepository compraRepository,
                        ConsumoPartidaRepository consumoPartidaRepository, BilleteraRepository billeteraRepository,
                        ProyectoRepository proyectoRepository, ProcesarCompraService procesarCompraService) {
                this.compraRepository = compraRepository;
                this.consumoPartidaRepository = consumoPartidaRepository;
                this.billeteraRepository = billeteraRepository;
                this.proyectoRepository = proyectoRepository;
                this.procesarCompraService = procesarCompraService;
        }

        @Override
        @Transactional
        public RegistrarCompraResponse registrar(RegistrarCompraCommand command) {
                // 1. Validar que el proyecto exista
                ProyectoId proyectoId = ProyectoId.from(command.proyectoId());
                if (proyectoRepository.findById(proyectoId).isEmpty()) {
                        throw new ProyectoNoEncontradoException(command.proyectoId());
                }

                // 2. Buscar la billetera del proyecto
                Billetera billetera = billeteraRepository.findByProyectoId(command.proyectoId())
                                .orElseThrow(() -> new BilleteraNoEncontradaException(command.proyectoId()));

                // 3. Crear la compra con sus detalles
                CompraId compraId = CompraId.nuevo();
                List<CompraDetalle> detalles = command.detalles().stream()
                                .map(detalleCommand -> CompraDetalle.crear(CompraDetalleId.nuevo(),
                                                detalleCommand.recursoExternalId(), detalleCommand.recursoNombre(),
                                                detalleCommand.unidad(), detalleCommand.partidaId(),
                                                detalleCommand.naturalezaGasto(), detalleCommand.relacionContractual(),
                                                detalleCommand.rubroInsumo(), detalleCommand.cantidad(),
                                                detalleCommand.precioUnitario()))
                                .collect(Collectors.toList());

                Compra compra = Compra.crear(compraId, command.proyectoId(), command.fecha(), command.proveedor(),
                                detalles);

                // 4. Procesar la compra (valida partidas, genera consumos, descuenta billetera)
                ProcesarCompraService.CompraProcesada resultado = procesarCompraService.procesar(compra, billetera);
                Compra compraAprobada = resultado.compra();
                List<ConsumoPartida> consumos = resultado.consumos();

                // 5. Persistir
                compraRepository.save(compraAprobada);
                billeteraRepository.save(billetera);
                consumoPartidaRepository.saveAll(consumos);

                // 6. Mapear detalles a respuesta
                List<CompraDetalleResponse> detallesResponse = compra.getDetalles().stream()
                                .map(detalle -> new CompraDetalleResponse(detalle.getId().getValue(),
                                                detalle.getRecursoExternalId(), detalle.getRecursoNombre(),
                                                detalle.getUnidad(), detalle.getPartidaId(),
                                                detalle.getNaturalezaGasto(), detalle.getRelacionContractual(),
                                                detalle.getRubroInsumo(), detalle.getCantidad(),
                                                detalle.getPrecioUnitario(), detalle.getSubtotal()))
                                .collect(Collectors.toList());

                // 7. Retornar respuesta
                return new RegistrarCompraResponse(compra.getId().getValue(), compra.getProyectoId(), compra.getFecha(),
                                compra.getProveedor(), compra.getEstado(), compra.getTotal(),
                                compra.getVersion().intValue(), detallesResponse, null, // createdAt se obtiene de la
                                                                                        // entidad después de persistir
                                null // updatedAt se obtiene de la entidad después de persistir
                );
        }
}
