package com.college.controller;

import com.college.model.Setting;
import com.college.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin/settings")
public class SettingController {

    @Autowired
    private SettingService settingService;

    /**
     * 获取所有系统设置
     */
    @GetMapping
    public ResponseEntity<List<Setting>> getAllSettings() {
        List<Setting> settings = settingService.getAll();
        return ResponseEntity.ok(settings);
    }

    /**
     * 根据分类获取系统设置
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Setting>> getSettingsByCategory(@PathVariable String category) {
        List<Setting> settings = settingService.getByCategory(category);
        return ResponseEntity.ok(settings);
    }

    /**
     * 根据键获取单个设置值
     */
    @GetMapping("/{key}")
    public ResponseEntity<?> getSettingByKey(@PathVariable String key) {
        Optional<Setting> setting = settingService.findByKey(key);
        if (setting.isPresent()) {
            return ResponseEntity.ok(setting.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 保存或更新系统设置
     */
    @PostMapping
    public ResponseEntity<Setting> saveSetting(@RequestBody Setting setting) {
        Setting savedSetting = settingService.save(setting);
        return ResponseEntity.ok(savedSetting);
    }

    /**
     * 批量保存或更新系统设置
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchSaveSettings(@RequestBody List<Setting> settings) {
        Map<String, Object> result = new HashMap<>();
        try {
            for (Setting setting : settings) {
                settingService.update(setting);
            }
            result.put("success", true);
            result.put("message", "设置保存成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 保存基本设置
     */
    @PutMapping("/basic")
    public ResponseEntity<Map<String, Object>> saveBasicSettings(@RequestBody Map<String, String> settings) {
        Map<String, Object> result = new HashMap<>();
        try {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                settingService.setValueByKey(entry.getKey(), entry.getValue());
            }
            result.put("success", true);
            result.put("message", "基本设置保存成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 获取基本设置
     */
    @GetMapping("/basic")
    public ResponseEntity<Map<String, String>> getBasicSettings() {
        Map<String, String> basicSettings = new HashMap<>();
        
        // 获取数据库中的值 (使用驼峰命名的键名)
        String systemNameValue = settingService.getValueByKey("systemName");
        String systemVersionValue = settingService.getValueByKey("systemVersion");
        String schoolNameValue = settingService.getValueByKey("schoolName");
        String contactEmailValue = settingService.getValueByKey("contactEmail");
        String contactPhoneValue = settingService.getValueByKey("contactPhone");
        String schoolAddressValue = settingService.getValueByKey("schoolAddress");
        String copyrightValue = settingService.getValueByKey("copyright");
        String systemDescriptionValue = settingService.getValueByKey("systemDescription");
        
        basicSettings.put("systemName", systemNameValue);
        basicSettings.put("systemVersion", systemVersionValue);
        basicSettings.put("schoolName", schoolNameValue);
        basicSettings.put("contactEmail", contactEmailValue);
        basicSettings.put("contactPhone", contactPhoneValue);
        basicSettings.put("schoolAddress", schoolAddressValue);
        basicSettings.put("copyright", copyrightValue);
        basicSettings.put("systemDescription", systemDescriptionValue);

        return ResponseEntity.ok(basicSettings);
    }
    
    /**
     * 获取所有系统设置（调试用）
     */
    @GetMapping("/all")
    public
    ResponseEntity<List<Setting>> getAllSettingsDebug() {
        List<Setting> settings = settingService.getAll();
        System.out.println("DEBUG - Total settings in DB: " + settings.size());
        for (Setting setting : settings) {
            System.out.println("DEBUG - Setting: " + setting.getSettingKey() + " = " + setting.getSettingValue());
        }
        return ResponseEntity.ok(settings);
    }
    
    /**
     * 获取特定设置的详细信息
     */
    @GetMapping("/debug/{key}")
    public ResponseEntity<?> getSettingDetail(@PathVariable String key) {
        System.out.println("DEBUG - Looking for setting with key: " + key);
        Optional<Setting> settingOpt = settingService.findByKey(key);
        if (settingOpt.isPresent()) {
            Setting setting = settingOpt.get();
            System.out.println("DEBUG - Found setting: " + setting.getSettingKey() + 
                ", value: '" + setting.getSettingValue() + "', type: " + 
                (setting.getSettingValue() != null ? setting.getSettingValue().getClass().getSimpleName() : "null"));
            return ResponseEntity.ok(setting);
        } else {
            System.out.println("DEBUG - Setting not found for key: " + key);
            // 检查数据库中所有的键名
            List<Setting> allSettings = settingService.getAll();
            System.out.println("DEBUG - Available keys in DB:");
            for (Setting s : allSettings) {
                System.out.println("DEBUG -   '" + s.getSettingKey() + "'");
            }
            return ResponseEntity.notFound().build();
        }
    }
}