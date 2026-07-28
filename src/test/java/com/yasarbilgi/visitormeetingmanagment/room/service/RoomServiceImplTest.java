package com.yasarbilgi.visitormeetingmanagment.room.service;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
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
import com.yasarbilgi.visitormeetingmanagment.room.service.impl.RoomServiceImpl;
import com.yasarbilgi.visitormeetingmanagment.security.util.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RoomServiceImpl için kapsamlı birim (unit) testler.
 *
 * Desen: Arrange (hazırlık) -> Act (işlemi çalıştır) -> Assert (doğrula)
 * Her metod için hem "mutlu yol" (başarılı senaryo) hem de olası hata
 * senaryoları ayrı test metodlarıyla kapsanıyor.
 */
@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private RoomServiceImpl roomService;

    private static final Long COMPANY_ID = 7L;
    private static final Long ROOM_ID = 1L;
    private static final Long FEATURE_ID = 10L;

    private Company company;
    private Feature feature;
    private Room room;
    private RoomResponseDto responseDto;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(COMPANY_ID)
                .name("Test Şirketi")
                .slug("test-sirketi")
                .build();

        feature = Feature.builder()
                .id(FEATURE_ID)
                .company(company)
                .name("Projeksiyon")
                .description("4K Projeksiyon")
                .build();

        room = Room.builder()
                .id(ROOM_ID)
                .company(company)
                .name("A Toplantı Odası")
                .location("1. Kat")
                .capacity(10)
                .description("Geniş toplantı odası")
                .features(new HashSet<>(Set.of(feature)))
                .build();

        responseDto = RoomResponseDto.builder()
                .id(ROOM_ID)
                .name("A Toplantı Odası")
                .location("1. Kat")
                .capacity(10)
                .description("Geniş toplantı odası")
                .active(true)
                .build();
    }

    // ----- create() -----

    @Test
    void create_shouldSucceed_whenRequestIsValidWithFeatures() {
        // Arrange
        RoomRequestDto dto = RoomRequestDto.builder()
                .name("A Toplantı Odası")
                .location("1. Kat")
                .capacity(10)
                .description("Geniş toplantı odası")
                .featureIds(Set.of(FEATURE_ID))
                .build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(roomRepository.existsByCompanyIdAndNameIgnoreCase(COMPANY_ID, dto.name())).thenReturn(false);
        when(featureRepository.findById(FEATURE_ID)).thenReturn(Optional.of(feature));
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.empty());
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toResponseDto(any(Room.class))).thenReturn(responseDto);

        // Act
        RoomResponseDto result = roomService.create(COMPANY_ID, dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("A Toplantı Odası");
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(auditLogService, times(1)).log(eq(COMPANY_ID), any(), eq("ROOM_CREATED"), eq("ROOM"), any(), anyString());
    }

    @Test
    void create_shouldSucceed_whenFeatureIdsIsNull() {
        // Arrange
        RoomRequestDto dto = RoomRequestDto.builder()
                .name("A Toplantı Odası")
                .location("1. Kat")
                .capacity(10)
                .featureIds(null)
                .build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(roomRepository.existsByCompanyIdAndNameIgnoreCase(COMPANY_ID, dto.name())).thenReturn(false);
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.empty());
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toResponseDto(any(Room.class))).thenReturn(responseDto);

        // Act
        RoomResponseDto result = roomService.create(COMPANY_ID, dto);

        // Assert
        assertThat(result).isNotNull();
        verify(featureRepository, never()).findById(anyLong());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void create_shouldThrowException_whenCompanyNotFound() {
        // Arrange
        RoomRequestDto dto = RoomRequestDto.builder()
                .name("A Toplantı Odası")
                .build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMPANY_NOT_FOUND);

        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void create_shouldThrowException_whenRoomNameAlreadyExists() {
        // Arrange
        RoomRequestDto dto = RoomRequestDto.builder()
                .name("A Toplantı Odası")
                .build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(roomRepository.existsByCompanyIdAndNameIgnoreCase(COMPANY_ID, dto.name())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> roomService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEETING_ROOM_ALREADY_EXISTS);

        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void create_shouldThrowException_whenFeatureNotFound() {
        // Arrange
        RoomRequestDto dto = RoomRequestDto.builder()
                .name("A Toplantı Odası")
                .featureIds(Set.of(FEATURE_ID))
                .build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(roomRepository.existsByCompanyIdAndNameIgnoreCase(COMPANY_ID, dto.name())).thenReturn(false);
        when(featureRepository.findById(FEATURE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEATURE_NOT_FOUND);

        verify(roomRepository, never()).save(any(Room.class));
    }

    // ----- update() -----

    @Test
    void update_shouldSucceed_whenNameUnchanged() {
        // Arrange
        UpdateRoomRequestDto dto = UpdateRoomRequestDto.builder()
                .name("A Toplantı Odası") // aynı isim
                .location("2. Kat")
                .capacity(12)
                .description("Güncellenmiş açıklama")
                .featureIds(Collections.emptySet())
                .build();

        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.empty());
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        // Act
        RoomResponseDto result = roomService.update(COMPANY_ID, ROOM_ID, dto);

        // Assert
        assertThat(result).isNotNull();
        verify(roomRepository, never()).existsByCompanyIdAndNameIgnoreCaseAndIdNot(anyLong(), anyString(), anyLong());
        verify(auditLogService, times(1)).log(eq(COMPANY_ID), any(), eq("ROOM_UPDATED"), eq("ROOM"), eq(ROOM_ID), anyString());
    }

    @Test
    void update_shouldValidateUniqueness_whenNameChanged() {
        // Arrange
        UpdateRoomRequestDto dto = UpdateRoomRequestDto.builder()
                .name("B Toplantı Odası")
                .location("2. Kat")
                .capacity(15)
                .featureIds(null)
                .build();

        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(roomRepository.existsByCompanyIdAndNameIgnoreCaseAndIdNot(COMPANY_ID, "B Toplantı Odası", ROOM_ID))
                .thenReturn(false);
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.empty());
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        // Act
        RoomResponseDto result = roomService.update(COMPANY_ID, ROOM_ID, dto);

        // Assert
        assertThat(result).isNotNull();
        verify(roomRepository, times(1)).existsByCompanyIdAndNameIgnoreCaseAndIdNot(COMPANY_ID, "B Toplantı Odası", ROOM_ID);
    }

    @Test
    void update_shouldThrowException_whenRoomNotFound() {
        // Arrange
        UpdateRoomRequestDto dto = UpdateRoomRequestDto.builder()
                .name("Yeni İsim")
                .build();

        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.update(COMPANY_ID, ROOM_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEETING_ROOM_NOT_FOUND);
    }

    @Test
    void update_shouldThrowException_whenNewNameAlreadyTaken() {
        // Arrange
        UpdateRoomRequestDto dto = UpdateRoomRequestDto.builder()
                .name("Başka Oda İsim")
                .build();

        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(roomRepository.existsByCompanyIdAndNameIgnoreCaseAndIdNot(COMPANY_ID, "Başka Oda İsim", ROOM_ID))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> roomService.update(COMPANY_ID, ROOM_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEETING_ROOM_ALREADY_EXISTS);
    }

    @Test
    void update_shouldThrowException_whenFeatureNotFound() {
        // Arrange
        UpdateRoomRequestDto dto = UpdateRoomRequestDto.builder()
                .name("A Toplantı Odası")
                .featureIds(Set.of(99L))
                .build();

        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(featureRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.update(COMPANY_ID, ROOM_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEATURE_NOT_FOUND);
    }

    // ----- getById() -----

    @Test
    void getById_shouldReturnRoomResponseDto_whenFound() {
        // Arrange
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        // Act
        RoomResponseDto result = roomService.getById(COMPANY_ID, ROOM_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("A Toplantı Odası");
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        // Arrange
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.getById(COMPANY_ID, ROOM_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEETING_ROOM_NOT_FOUND);
    }

    // ----- getAll() / getAllByActive() / search() -----

    @Test
    void getAll_shouldReturnPagedRooms() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Room> page = new PageImpl<>(List.of(room));

        when(roomRepository.findAllByCompanyId(COMPANY_ID, pageable)).thenReturn(page);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        // Act
        Page<RoomResponseDto> result = roomService.getAll(COMPANY_ID, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("A Toplantı Odası");
    }

    @Test
    void getAllByActive_shouldReturnFilteredPagedRooms() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Room> page = new PageImpl<>(List.of(room));

        when(roomRepository.findAllByCompanyIdAndActive(COMPANY_ID, true, pageable)).thenReturn(page);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        // Act
        Page<RoomResponseDto> result = roomService.getAllByActive(COMPANY_ID, true, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByMinimumCapacity_shouldReturnRooms_whenValidCapacity() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Room> page = new PageImpl<>(List.of(room));

        when(roomRepository.findAllByCompanyIdAndActiveAndCapacityGreaterThanEqual(COMPANY_ID, true, 5, pageable))
                .thenReturn(page);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        // Act
        Page<RoomResponseDto> result = roomService.getAllByMinimumCapacity(COMPANY_ID, true, 5, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByMinimumCapacity_shouldThrowException_whenCapacityIsInvalid() {
        // Arrange
        Pageable pageable = Pageable.unpaged();

        // Act & Assert
        assertThatThrownBy(() -> roomService.getAllByMinimumCapacity(COMPANY_ID, true, 0, pageable))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ROOM_CAPACITY);

        verify(roomRepository, never()).findAllByCompanyIdAndActiveAndCapacityGreaterThanEqual(anyLong(), anyBoolean(), anyInt(), any());
    }

    @Test
    void search_shouldReturnMatchingRooms() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Room> page = new PageImpl<>(List.of(room));

        when(roomRepository.searchByKeyword(COMPANY_ID, true, "Toplantı", pageable)).thenReturn(page);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        // Act
        Page<RoomResponseDto> result = roomService.search(COMPANY_ID, true, " Toplantı ", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        verify(roomRepository, times(1)).searchByKeyword(COMPANY_ID, true, "Toplantı", pageable);
    }

    // ----- addFeature() / removeFeature() -----

    @Test
    void addFeature_shouldSucceed_whenRoomAndFeatureExist() {
        // Arrange
        Feature newFeature = Feature.builder()
                .id(20L)
                .company(company)
                .name("Beyaz Tahta")
                .build();

        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(featureRepository.findById(20L)).thenReturn(Optional.of(newFeature));
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        // Act
        RoomResponseDto result = roomService.addFeature(COMPANY_ID, ROOM_ID, 20L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(room.getFeatures()).contains(newFeature);
    }

    @Test
    void addFeature_shouldThrowException_whenRoomNotFound() {
        // Arrange
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.addFeature(COMPANY_ID, ROOM_ID, FEATURE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEETING_ROOM_NOT_FOUND);
    }

    @Test
    void addFeature_shouldThrowException_whenFeatureNotFound() {
        // Arrange
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(featureRepository.findById(FEATURE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.addFeature(COMPANY_ID, ROOM_ID, FEATURE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEATURE_NOT_FOUND);
    }

    @Test
    void removeFeature_shouldSucceed_whenRoomAndFeatureExist() {
        // Arrange
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(featureRepository.findById(FEATURE_ID)).thenReturn(Optional.of(feature));
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        // Act
        RoomResponseDto result = roomService.removeFeature(COMPANY_ID, ROOM_ID, FEATURE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(room.getFeatures()).doesNotContain(feature);
    }

    @Test
    void removeFeature_shouldThrowException_whenRoomNotFound() {
        // Arrange
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.removeFeature(COMPANY_ID, ROOM_ID, FEATURE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEETING_ROOM_NOT_FOUND);
    }

    @Test
    void removeFeature_shouldThrowException_whenFeatureNotFound() {
        // Arrange
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(featureRepository.findById(FEATURE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.removeFeature(COMPANY_ID, ROOM_ID, FEATURE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEATURE_NOT_FOUND);
    }

    // ----- deactivate() / activate() -----

    @Test
    void deactivate_shouldSucceed_whenRoomExists() {
        // Arrange
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        roomService.deactivate(COMPANY_ID, ROOM_ID);

        // Assert
        assertThat(room.isActive()).isFalse();
        verify(auditLogService, times(1)).log(eq(COMPANY_ID), any(), eq("ROOM_DEACTIVATED"), eq("ROOM"), eq(ROOM_ID), anyString());
    }

    @Test
    void deactivate_shouldThrowException_whenRoomNotFound() {
        // Arrange
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.deactivate(COMPANY_ID, ROOM_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEETING_ROOM_NOT_FOUND);
    }

    @Test
    void activate_shouldSucceed_whenRoomExists() {
        // Arrange
        room.deactivate(); // pasif hale getir
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.of(room));
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        roomService.activate(COMPANY_ID, ROOM_ID);

        // Assert
        assertThat(room.isActive()).isTrue();
        verify(auditLogService, times(1)).log(eq(COMPANY_ID), any(), eq("ROOM_ACTIVATED"), eq("ROOM"), eq(ROOM_ID), anyString());
    }

    @Test
    void activate_shouldThrowException_whenRoomNotFound() {
        // Arrange
        when(roomRepository.findByIdAndCompanyId(ROOM_ID, COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roomService.activate(COMPANY_ID, ROOM_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEETING_ROOM_NOT_FOUND);
    }
}
