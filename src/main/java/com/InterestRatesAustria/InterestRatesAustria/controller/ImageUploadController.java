package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.service.CloudinaryService;
import com.InterestRatesAustria.InterestRatesAustria.service.FieldValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private FieldValueService fieldValueService;

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam("rateId") Long rateId,
            @RequestParam("fieldId") Long fieldId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get existing image URL if any
            String oldImageUrl = null;
            if (fieldValueService.existsFieldValue(rateId, fieldId)) {
                Map<Long, String> fieldValues = fieldValueService.getFieldValuesForRate(rateId);
                oldImageUrl = fieldValues.get(fieldId);
            }

            // Upload new image to Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file, "interest_rates");

            // Delete old image if it exists
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                try {
                    cloudinaryService.deleteImage(oldImageUrl);
                } catch (Exception e) {
                    // Log but don't fail if old image deletion fails
                    System.err.println("Failed to delete old image: " + e.getMessage());
                }
            }

            // Update database with new image URL
            if (fieldValueService.existsFieldValue(rateId, fieldId)) {
                fieldValueService.updateFieldValue(rateId, fieldId, imageUrl);
            } else {
                fieldValueService.createFieldValue(rateId, fieldId, imageUrl);
            }

            response.put("success", true);
            response.put("imagePath", imageUrl);
            response.put("message", "Image uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error uploading file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/delete-image")
    public ResponseEntity<Map<String, Object>> deleteImage(
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long rateId = Long.valueOf(requestBody.get("rateId").toString());
            Long fieldId = Long.valueOf(requestBody.get("fieldId").toString());

            // Get current image URL
            Map<Long, String> fieldValues = fieldValueService.getFieldValuesForRate(rateId);
            String imageUrl = fieldValues.get(fieldId);

            // Delete from Cloudinary
            if (imageUrl != null && !imageUrl.isEmpty()) {
                cloudinaryService.deleteImage(imageUrl);
            }

            // Clear from database
            fieldValueService.updateFieldValue(rateId, fieldId, "");
            
            response.put("success", true);
            response.put("message", "Image deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}