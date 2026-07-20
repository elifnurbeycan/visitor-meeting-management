package com.yasarbilgi.visitormeetingmanagment.room.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.room.dto.request.RoomRequestDto;
import com.yasarbilgi.visitormeetingmanagment.room.dto.request.UpdateRoomRequestDto;
import com.yasarbilgi.visitormeetingmanagment.room.dto.response.RoomResponseDto;
import com.yasarbilgi.visitormeetingmanagment.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Room kaynağı için REST endpoint'leri.
 * URL şeması: /api/v1/rooms
 *
 * Security ve tenant context henüz eklenmediği için
 * companyId request parametresi olarak alınmaktadır.
 */
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * Belirtilen şirket için yeni bir toplantı odası oluşturur.
     * Başarılı olursa 201 Created döner.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponseDto>> create(
            @RequestParam Long companyId,
            @Valid @RequestBody RoomRequestDto dto
    ) {
        RoomResponseDto created = roomService.create(companyId, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Room created successfully",
                        created
                ));
    }

    /**
     * Var olan bir toplantı odasını günceller.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> update(
            @PathVariable Long id,
            @RequestParam Long companyId,
            @Valid @RequestBody UpdateRoomRequestDto dto
    ) {
        RoomResponseDto updated =
                roomService.update(companyId, id, dto);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Room updated successfully",
                        updated
                )
        );
    }

    /**
     * ID ve şirket bilgisine göre tek bir toplantı odası getirir.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> getById(
            @PathVariable Long id,
            @RequestParam Long companyId
    ) {
        RoomResponseDto room =
                roomService.getById(companyId, id);

        return ResponseEntity.ok(
                ApiResponse.success(room)
        );
    }

    /**
     * Belirtilen şirkete ait bütün toplantı odalarını
     * sayfalanmış şekilde getirir.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RoomResponseDto>>> getAll(
            @RequestParam Long companyId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<RoomResponseDto> rooms =
                PageResponse.of(
                        roomService.getAll(companyId, pageable)
                );

        return ResponseEntity.ok(
                ApiResponse.success(rooms)
        );
    }

    /**
     * Aktif veya pasif odaları sayfalanmış şekilde listeler.
     */
    @GetMapping("/by-active")
    public ResponseEntity<ApiResponse<PageResponse<RoomResponseDto>>> getAllByActive(
            @RequestParam Long companyId,
            @RequestParam boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<RoomResponseDto> rooms =
                PageResponse.of(
                        roomService.getAllByActive(
                                companyId,
                                active,
                                pageable
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(rooms)
        );
    }

    /**
     * Belirtilen minimum kapasiteyi karşılayan odaları listeler.
     *
     * active parametresi verilmezse yalnızca aktif odalar getirilir.
     */
    @GetMapping("/by-capacity")
    public ResponseEntity<ApiResponse<PageResponse<RoomResponseDto>>> getAllByMinimumCapacity(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam int capacity,
            @PageableDefault(size = 20, sort = "capacity") Pageable pageable
    ) {
        PageResponse<RoomResponseDto> rooms =
                PageResponse.of(
                        roomService.getAllByMinimumCapacity(
                                companyId,
                                active,
                                capacity,
                                pageable
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(rooms)
        );
    }

    /**
     * Oda adı, konumu veya açıklaması üzerinde
     * anahtar kelime araması yapar.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<RoomResponseDto>>> search(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<RoomResponseDto> rooms =
                PageResponse.of(
                        roomService.search(
                                companyId,
                                active,
                                keyword,
                                pageable
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(rooms)
        );
    }

    /**
     * Bir toplantı odasına yeni bir özellik ekler.
     */
    @PatchMapping("/{roomId}/features/{featureId}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> addFeature(
            @PathVariable Long roomId,
            @PathVariable Long featureId,
            @RequestParam Long companyId
    ) {
        RoomResponseDto updated =
                roomService.addFeature(
                        companyId,
                        roomId,
                        featureId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Feature added to room successfully",
                        updated
                )
        );
    }

    /**
     * Bir toplantı odasından özellik kaldırır.
     */
    @DeleteMapping("/{roomId}/features/{featureId}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> removeFeature(
            @PathVariable Long roomId,
            @PathVariable Long featureId,
            @RequestParam Long companyId
    ) {
        RoomResponseDto updated =
                roomService.removeFeature(
                        companyId,
                        roomId,
                        featureId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Feature removed from room successfully",
                        updated
                )
        );
    }

    /**
     * Bir toplantı odasını pasif hâle getirir.
     * Kayıt veritabanından silinmez.
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long id,
            @RequestParam Long companyId
    ) {
        roomService.deactivate(companyId, id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Room deactivated successfully"
                )
        );
    }

    /**
     * Pasif durumdaki bir toplantı odasını tekrar aktif hâle getirir.
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable Long id,
            @RequestParam Long companyId
    ) {
        roomService.activate(companyId, id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Room activated successfully"
                )
        );
    }
}