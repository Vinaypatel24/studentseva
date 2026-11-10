package com.studentseva.businesses;

import com.studentseva.businesses.dto.NearbyBusinessProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Business operations and PostGIS spatial queries.
 */
@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    /**
     * Finds the business owned by a specific user.
     */
    Optional<Business> findByOwnerId(UUID ownerId);

    /**
     * Checks if a user already owns a business.
     */
    boolean existsByOwnerId(UUID ownerId);

    /**
     * Finds businesses filtered by category.
     */
    List<Business> findByCategory(Business.Category category);

    /**
     * PostGIS Spatial Query: Finds all OPEN businesses within a geodesic radius (meters)
     * of the given (latitude, longitude) coordinate.
     * Utilizes the GiST spatial index on b.location for high performance.
     */
    @Query(value = """
        SELECT b.id AS id,
               b.owner_id AS ownerId,
               u.name AS ownerName,
               b.name AS name,
               b.category AS category,
               b.description AS description,
               b.address AS address,
               ST_Y(b.location::geometry) AS latitude,
               ST_X(b.location::geometry) AS longitude,
               b.phone AS phone,
               b.whatsapp AS whatsapp,
               b.status AS status,
               b.verification_status AS verificationStatus,
               ROUND(ST_Distance(b.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)::numeric, 1) AS distanceMeters,
               b.created_at AS createdAt
        FROM businesses b
        JOIN users u ON b.owner_id = u.id
        WHERE b.status = 'OPEN'
          AND ST_DWithin(b.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters)
        ORDER BY distanceMeters ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<NearbyBusinessProjection> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") double radiusMeters,
            @Param("limit") int limit
    );

    /**
     * PostGIS Spatial Query filtered by specific business category (e.g. HOSTEL_PG, MESS_TIFFIN).
     */
    @Query(value = """
        SELECT b.id AS id,
               b.owner_id AS ownerId,
               u.name AS ownerName,
               b.name AS name,
               b.category AS category,
               b.description AS description,
               b.address AS address,
               ST_Y(b.location::geometry) AS latitude,
               ST_X(b.location::geometry) AS longitude,
               b.phone AS phone,
               b.whatsapp AS whatsapp,
               b.status AS status,
               b.verification_status AS verificationStatus,
               ROUND(ST_Distance(b.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)::numeric, 1) AS distanceMeters,
               b.created_at AS createdAt
        FROM businesses b
        JOIN users u ON b.owner_id = u.id
        WHERE b.status = 'OPEN'
          AND b.category = :category
          AND ST_DWithin(b.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters)
        ORDER BY distanceMeters ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<NearbyBusinessProjection> findNearbyByCategory(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") double radiusMeters,
            @Param("category") String category,
            @Param("limit") int limit
    );
}
