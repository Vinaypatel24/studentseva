package com.studentseva.businesses;

import com.studentseva.businesses.dto.BusinessResponse;
import com.studentseva.businesses.dto.CreateBusinessRequest;
import com.studentseva.common.exceptions.NotFoundException;
import com.studentseva.users.User;
import com.studentseva.users.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service managing business lifecycle and spatial coordinates conversion.
 */
@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    /**
     * JTS GeometryFactory initialized with SRID 4326 (WGS 84 GPS standard).
     */
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public BusinessService(BusinessRepository businessRepository, UserRepository userRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
    }

    /**
     * Registers a new business listing for the authenticated user.
     * Upgrades the user's role to VENDOR if they were previously STUDENT.
     */
    @Transactional
    public BusinessResponse createBusiness(CreateBusinessRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("Owner user not found with email: " + ownerEmail));

        if (businessRepository.existsByOwnerId(owner.getId())) {
            throw new IllegalStateException("You already have a registered business. Only one business per account is allowed currently.");
        }

        // In JTS Geometry: X = Longitude, Y = Latitude
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));

        Business business = new Business();
        business.setOwner(owner);
        business.setName(request.name().trim());
        business.setCategory(request.category());
        business.setDescription(request.description() != null ? request.description().trim() : null);
        business.setAddress(request.address().trim());
        business.setLocation(location);
        business.setPhone(request.phone() != null ? request.phone().trim() : null);
        business.setWhatsapp(request.whatsapp() != null ? request.whatsapp().trim() : null);

        Business saved = businessRepository.save(business);

        // Auto-upgrade role to VENDOR if currently STUDENT
        if (owner.getRole() == User.Role.STUDENT) {
            owner.setRole(User.Role.VENDOR);
            userRepository.save(owner);
        }

        return toBusinessResponse(saved);
    }

    /**
     * Retrieves the business belonging to the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public BusinessResponse getMyBusiness(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + ownerEmail));

        Business business = businessRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new NotFoundException("No business listing found for your account."));

        return toBusinessResponse(business);
    }

    /**
     * Retrieves a business by its UUID.
     */
    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(UUID id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Business not found with ID: " + id));

        return toBusinessResponse(business);
    }

    /**
     * Maps a Business entity to a clean API response DTO.
     */
    public BusinessResponse toBusinessResponse(Business b) {
        // In JTS Point: getX() is Longitude, getY() is Latitude
        Double longitude = b.getLocation() != null ? b.getLocation().getX() : null;
        Double latitude = b.getLocation() != null ? b.getLocation().getY() : null;

        return new BusinessResponse(
                b.getId(),
                b.getOwner().getId(),
                b.getOwner().getName(),
                b.getName(),
                b.getCategory().name(),
                b.getDescription(),
                b.getAddress(),
                latitude,
                longitude,
                b.getPhone(),
                b.getWhatsapp(),
                b.getStatus().name(),
                b.getVerificationStatus().name(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}

