package com.InterestRatesAustria.InterestRatesAustria.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_FORMATS = {"jpg", "jpeg", "png", "gif", "webp"};

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload image to Cloudinary
     * @param file MultipartFile to upload
     * @param folder Folder name in Cloudinary (e.g., "interest_rates", "carousel")
     * @return Cloudinary URL of uploaded image
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        validateFile(file);

        String publicId = generatePublicId(folder);

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
            ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "image",
                "overwrite", false,
                "transformation", new com.cloudinary.Transformation()
                    .quality("auto")
                    .fetchFormat("auto")
            )
        );

        return (String) uploadResult.get("secure_url");
    }

    /**
     * Delete image from Cloudinary
     * @param imageUrl Full Cloudinary URL
     */
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        String publicId = extractPublicIdFromUrl(imageUrl);
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    /**
     * Update existing image (delete old, upload new)
     */
    public String updateImage(String oldImageUrl, MultipartFile newFile, String folder) throws IOException {
        // Delete old image
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            try {
                deleteImage(oldImageUrl);
            } catch (Exception e) {
                // Log but don't fail if old image deletion fails
                System.err.println("Failed to delete old image: " + e.getMessage());
            }
        }

        // Upload new image
        return uploadImage(newFile, folder);
    }

    /**
     * Extract public_id from Cloudinary URL
     * Example: https://res.cloudinary.com/demo/image/upload/v1234/folder/image.jpg
     * Returns: folder/image
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // Extract public_id from URL
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String afterUpload = parts[1];
            // Remove version number (v1234567890/)
            String withoutVersion = afterUpload.replaceFirst("v\\d+/", "");
            // Remove file extension
            int lastDot = withoutVersion.lastIndexOf('.');
            if (lastDot > 0) {
                withoutVersion = withoutVersion.substring(0, lastDot);
            }

            return withoutVersion;
        } catch (Exception e) {
            System.err.println("Failed to extract public_id from URL: " + imageUrl);
            return null;
        }
    }

    /**
     * Generate unique public ID for image
     */
    private String generatePublicId(String folder) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return folder + "_" + timestamp + "_" + uuid;
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Please select a file to upload");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Please upload a valid image file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            boolean isAllowed = false;
            for (String allowedFormat : ALLOWED_FORMATS) {
                if (extension.equals(allowedFormat)) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                throw new IOException("Only JPG, JPEG, PNG, GIF, and WEBP files are allowed");
            }
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot + 1);
    }
}