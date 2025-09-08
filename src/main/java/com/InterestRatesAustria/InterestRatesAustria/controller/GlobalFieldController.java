package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.service.GlobalFieldService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/fields")
public class GlobalFieldController {

    private final GlobalFieldService globalFieldService;

    public GlobalFieldController(GlobalFieldService globalFieldService) {
        this.globalFieldService = globalFieldService;
    }

    @PostMapping("/add")
    public String addGlobalField(@ModelAttribute GlobalField field) {
        globalFieldService.addGlobalField(field);
        return "redirect:/admin";
    }

    @GetMapping("/createMultiple")
    public String createMultipleGlobalFields(@ModelAttribute GlobalField field) {
        return "admin/createGlobalField";
    }

    @PostMapping("/update")
    public String updateGlobalField(@RequestParam Long fieldId,
                                    @RequestParam String label) {
        globalFieldService.updateGlobalField(fieldId, label);
        return "redirect:/admin";
    }

    @PostMapping("/reorder")
    @ResponseBody
    public ResponseEntity<String> reorderFields(@RequestBody List<Long> fieldIds) {
        try {
            globalFieldService.reorderGlobalFields(fieldIds);
            return ResponseEntity.ok("Order updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating order: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteGlobalField(@PathVariable Long id) {
        try {
            globalFieldService.deleteGlobalField(id);
            return ResponseEntity.ok("Field deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting field: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteGlobalFieldRest(@PathVariable Long id) {
        try {
            globalFieldService.deleteGlobalField(id);
            return ResponseEntity.ok("Field deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting field: " + e.getMessage());
        }
    }

    @PostMapping("/add-multiple")
    public String addMultipleGlobalFields(@RequestParam Map<String, String> allParams) {
        globalFieldService.addMultipleGlobalFields(allParams);
        return "redirect:/admin";
    }

    @GetMapping("/editMultiple")
    public String editMultipleGlobalFields(Map<String, Object> model) {
        List<GlobalField> fields = globalFieldService.getAllGlobalFieldsOrdered();
        model.put("fields", fields);
        return "admin/editGlobalField";
    }

    @PostMapping("/update-multiple")
    public String updateMultipleGlobalFields(@RequestParam Map<String, String> allParams) {
        globalFieldService.updateMultipleGlobalFields(allParams);
        return "redirect:/admin";
    }
}