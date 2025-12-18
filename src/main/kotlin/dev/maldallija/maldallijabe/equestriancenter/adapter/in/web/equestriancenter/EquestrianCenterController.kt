package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.equestriancenter

import dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.equestriancenter.dto.EquestrianCenterListResponse
import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.GetEquestrianCentersUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "EquestrianCenter", description = "승마장 API")
@RestController
@RequestMapping("/api/v1/equestrian-centers")
class EquestrianCenterController(
    private val getEquestrianCentersUseCase: GetEquestrianCentersUseCase,
) {
    @Operation(summary = "승마장 목록 조회")
    @GetMapping
    fun getEquestrianCenters(
        @PageableDefault(size = 20, sort = ["name"], direction = Sort.Direction.ASC) pageable: Pageable,
    ): ResponseEntity<Page<EquestrianCenterListResponse>> {
        val equestrianCenters = getEquestrianCentersUseCase.getEquestrianCenters(pageable)

        val response =
            equestrianCenters.map { equestrianCenter ->
                EquestrianCenterListResponse(
                    uuid = equestrianCenter.uuid,
                    name = equestrianCenter.name,
                    description = equestrianCenter.description,
                )
            }

        return ResponseEntity.ok(response)
    }
}
