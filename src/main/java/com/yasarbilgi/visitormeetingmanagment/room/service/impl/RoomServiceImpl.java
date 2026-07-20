package com.yasarbilgi.visitormeetingmanagment.room.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.feature.entity.Feature;
import com.yasarbilgi.visitormeetingmanagment.feature.repository.FeatureRepository;
import com.yasarbilgi.visitormeetingmanagment.room.dto.request.RoomRequestDto;
import com.yasarbilgi.visitormeetingmanagment.room.dto.request.UpdateRoomRequestDto;
import com.yasarbilgi.visitormeetingmanagment.room.dto.response.RoomResponseDto;
import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import com.yasarbilgi.visitormeetingmanagment.room.mapper.RoomMapper;
import com.yasarbilgi.visitormeetingmanagment.room.repository.RoomRepository;
import com.yasarbilgi.visitormeetingmanagment.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final CompanyRepository companyRepository;
    private final FeatureRepository featureRepository;

    /**
     * Belirtilen şirket için yeni bir toplantı odası oluşturur.
     *
     * Aynı şirket içerisinde aynı isimde başka bir oda bulunamaz.
     * DTO içerisinde feature ID'leri gönderilmişse özellikler bulunarak
     * yeni odaya atanır.
     */
    @Override
    @Transactional
    public RoomResponseDto create(
            Long companyId,
            RoomRequestDto dto
    ) {
        log.info(
                "Creating room with name: {} for company id: {}",
                dto.name(),
                companyId
        );

        Company company = findCompanyOrThrow(companyId);

        validateRoomNameNotTaken(
                companyId,
                dto.name()
        );

        Set<Feature> features =
                findFeaturesOrThrow(dto.featureIds());

        Room room = Room.builder()
                .company(company)
                .name(dto.name())
                .location(dto.location())
                .capacity(dto.capacity())
                .description(dto.description())
                .features(new HashSet<>(features))
                .build();

        Room saved = roomRepository.save(room);

        log.info(
                "Room created successfully with id: {} for company id: {}",
                saved.getId(),
                companyId
        );

        return roomMapper.toResponseDto(saved);
    }

    /**
     * Belirtilen şirkete ait mevcut bir toplantı odasını günceller.
     *
     * Oda adı değişmişse aynı şirket içerisinde benzersizlik kontrolü yapılır.
     * Feature listesi DTO'dan gelen güncel değerlerle değiştirilir.
     */
    @Override
    @Transactional
    public RoomResponseDto update(
            Long companyId,
            Long id,
            UpdateRoomRequestDto dto
    ) {
        log.info(
                "Updating room with id: {} for company id: {}",
                id,
                companyId
        );

        Room room = findRoomOrThrow(
                companyId,
                id
        );

        if (!room.getName().equalsIgnoreCase(dto.name())) {
            validateRoomNameNotTakenForUpdate(
                    companyId,
                    dto.name(),
                    id
            );
        }

        Set<Feature> requestedFeatures =
                findFeaturesOrThrow(dto.featureIds());

        room.rename(dto.name());
        room.updateLocation(dto.location());
        room.updateCapacity(dto.capacity());
        room.updateDescription(dto.description());

        updateRoomFeatures(
                room,
                requestedFeatures
        );

        log.info(
                "Room updated successfully with id: {} for company id: {}",
                id,
                companyId
        );

        return roomMapper.toResponseDto(room);
    }

    /**
     * ID ve şirket bilgisine göre tek bir toplantı odasını getirir.
     */
    @Override
    public RoomResponseDto getById(
            Long companyId,
            Long id
    ) {
        log.debug(
                "Fetching room with id: {} for company id: {}",
                id,
                companyId
        );

        Room room = findRoomOrThrow(
                companyId,
                id
        );

        return roomMapper.toResponseDto(room);
    }

    /**
     * Belirtilen şirkete ait bütün toplantı odalarını sayfalanmış şekilde getirir.
     */
    @Override
    public Page<RoomResponseDto> getAll(
            Long companyId,
            Pageable pageable
    ) {
        log.debug(
                "Fetching all rooms for company id: {}, page: {}",
                companyId,
                pageable
        );

        return roomRepository
                .findAllByCompanyId(companyId, pageable)
                .map(roomMapper::toResponseDto);
    }

    /**
     * Belirtilen şirkete ait odaları aktiflik durumuna göre filtreler.
     */
    @Override
    public Page<RoomResponseDto> getAllByActive(
            Long companyId,
            boolean active,
            Pageable pageable
    ) {
        log.debug(
                "Fetching rooms for company id: {}, active: {}, page: {}",
                companyId,
                active,
                pageable
        );

        return roomRepository
                .findAllByCompanyIdAndActive(
                        companyId,
                        active,
                        pageable
                )
                .map(roomMapper::toResponseDto);
    }

    /**
     * Belirtilen minimum kapasiteyi karşılayan odaları getirir.
     */
    @Override
    public Page<RoomResponseDto> getAllByMinimumCapacity(
            Long companyId,
            boolean active,
            int capacity,
            Pageable pageable
    ) {
        log.debug(
                "Fetching rooms for company id: {}, active: {}, minimum capacity: {}",
                companyId,
                active,
                capacity
        );

        if (capacity < 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_ROOM_CAPACITY
            );
        }

        return roomRepository
                .findAllByCompanyIdAndActiveAndCapacityGreaterThanEqual(
                        companyId,
                        active,
                        capacity,
                        pageable
                )
                .map(roomMapper::toResponseDto);
    }

    /**
     * Oda adı, konumu veya açıklaması üzerinde anahtar kelime araması yapar.
     */
    @Override
    public Page<RoomResponseDto> search(
            Long companyId,
            boolean active,
            String keyword,
            Pageable pageable
    ) {
        log.debug(
                "Searching rooms for company id: {}, keyword: '{}', active: {}",
                companyId,
                keyword,
                active
        );

        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        return roomRepository
                .searchByKeyword(
                        companyId,
                        active,
                        normalizedKeyword,
                        pageable
                )
                .map(roomMapper::toResponseDto);
    }

    /**
     * Bir toplantı odasına yeni bir özellik ekler.
     */
    @Override
    @Transactional
    public RoomResponseDto addFeature(
            Long companyId,
            Long roomId,
            Long featureId
    ) {
        log.info(
                "Adding feature id: {} to room id: {} for company id: {}",
                featureId,
                roomId,
                companyId
        );

        Room room = findRoomOrThrow(
                companyId,
                roomId
        );

        Feature feature =
                findFeatureOrThrow(featureId);

        room.addFeature(feature);

        log.info(
                "Feature id: {} added successfully to room id: {}",
                featureId,
                roomId
        );

        return roomMapper.toResponseDto(room);
    }

    /**
     * Bir toplantı odasından özellik kaldırır.
     */
    @Override
    @Transactional
    public RoomResponseDto removeFeature(
            Long companyId,
            Long roomId,
            Long featureId
    ) {
        log.info(
                "Removing feature id: {} from room id: {} for company id: {}",
                featureId,
                roomId,
                companyId
        );

        Room room = findRoomOrThrow(
                companyId,
                roomId
        );

        Feature feature =
                findFeatureOrThrow(featureId);

        room.removeFeature(feature);

        log.info(
                "Feature id: {} removed successfully from room id: {}",
                featureId,
                roomId
        );

        return roomMapper.toResponseDto(room);
    }

    /**
     * Bir toplantı odasını pasif hale getirir.
     */
    @Override
    @Transactional
    public void deactivate(
            Long companyId,
            Long id
    ) {
        log.info(
                "Deactivating room with id: {} for company id: {}",
                id,
                companyId
        );

        Room room = findRoomOrThrow(
                companyId,
                id
        );

        room.deactivateIfAllowed();

        log.info(
                "Room deactivated successfully with id: {}",
                id
        );
    }

    /**
     * Pasif durumdaki bir toplantı odasını tekrar aktif hale getirir.
     */
    @Override
    @Transactional
    public void activate(
            Long companyId,
            Long id
    ) {
        log.info(
                "Activating room with id: {} for company id: {}",
                id,
                companyId
        );

        Room room = findRoomOrThrow(
                companyId,
                id
        );

        room.activate();

        log.info(
                "Room activated successfully with id: {}",
                id
        );
    }

    // ----- Private helpers -----

    /**
     * ID ve şirket ID bilgisine göre toplantı odasını bulur.
     */
    private Room findRoomOrThrow(
            Long companyId,
            Long roomId
    ) {
        return roomRepository
                .findByIdAndCompanyId(
                        roomId,
                        companyId
                )
                .orElseThrow(() -> {
                    log.warn(
                            "Room not found with id: {} for company id: {}",
                            roomId,
                            companyId
                    );

                    return new BusinessException(
                            ErrorCode.MEETING_ROOM_NOT_FOUND
                    );
                });
    }

    /**
     * ID bilgisine göre şirketi bulur.
     */
    private Company findCompanyOrThrow(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(() -> {
                    log.warn(
                            "Company not found with id: {}",
                            companyId
                    );

                    return new BusinessException(
                            ErrorCode.COMPANY_NOT_FOUND
                    );
                });
    }

    /**
     * ID bilgisine göre oda özelliğini bulur.
     */
    private Feature findFeatureOrThrow(Long featureId) {
        return featureRepository
                .findById(featureId)
                .orElseThrow(() -> {
                    log.warn(
                            "Feature not found with id: {}",
                            featureId
                    );

                    return new BusinessException(
                            ErrorCode.FEATURE_NOT_FOUND
                    );
                });
    }

    /**
     * Gönderilen ID listesindeki bütün özellikleri getirir.
     *
     * ID listesi boşsa boş Set döndürür.
     * Gönderilen ID sayısıyla bulunan özellik sayısı farklıysa
     * en az bir özellik bulunamamış demektir.
     */
    private Set<Feature> findFeaturesOrThrow(
            Set<Long> featureIds
    ) {
        if (featureIds == null || featureIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Feature> features =
                new HashSet<>(
                        featureRepository.findAllById(featureIds)
                );

        if (features.size() != featureIds.size()) {
            log.warn(
                    "One or more features could not be found. Requested ids: {}",
                    featureIds
            );

            throw new BusinessException(
                    ErrorCode.FEATURE_NOT_FOUND
            );
        }

        return features;
    }

    /**
     * Yeni oda oluşturulurken aynı şirkette aynı isimde oda bulunup
     * bulunmadığını kontrol eder.
     */
    private void validateRoomNameNotTaken(
            Long companyId,
            String name
    ) {
        if (roomRepository.existsByCompanyIdAndNameIgnoreCase(
                companyId,
                name
        )) {
            throw new BusinessException(
                    ErrorCode.MEETING_ROOM_ALREADY_EXISTS
            );
        }
    }

    /**
     * Oda güncellenirken mevcut oda hariç aynı isimde başka oda
     * bulunup bulunmadığını kontrol eder.
     */
    private void validateRoomNameNotTakenForUpdate(
            Long companyId,
            String name,
            Long roomId
    ) {
        if (roomRepository
                .existsByCompanyIdAndNameIgnoreCaseAndIdNot(
                        companyId,
                        name,
                        roomId
                )) {
            throw new BusinessException(
                    ErrorCode.MEETING_ROOM_ALREADY_EXISTS
            );
        }
    }

    /**
     * Odanın mevcut özelliklerini DTO'dan gelen özellik listesiyle eşitler.
     *
     * DTO'da artık bulunmayan özellikler kaldırılır,
     * yeni gönderilen özellikler eklenir.
     */
    private void updateRoomFeatures(
            Room room,
            Set<Feature> requestedFeatures
    ) {
        Set<Feature> currentFeatures =
                new HashSet<>(room.getFeatures());

        currentFeatures.stream()
                .filter(feature ->
                        !requestedFeatures.contains(feature)
                )
                .forEach(room::removeFeature);

        requestedFeatures.stream()
                .filter(feature ->
                        !room.hasFeature(feature)
                )
                .forEach(room::addFeature);
    }
}