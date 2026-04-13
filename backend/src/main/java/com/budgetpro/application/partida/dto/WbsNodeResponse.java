package com.budgetpro.application.partida.dto;

import java.util.List;

public record WbsNodeResponse(
        PartidaResponse partida,
        List<WbsNodeResponse> children
) {
}

