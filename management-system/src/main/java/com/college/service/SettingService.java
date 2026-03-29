package com.college.service;

import com.college.model.Setting;
import com.college.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SettingService {

    @Autowired
    private SettingRepository settingRepository;

    public Optional<Setting> findByKey(String settingKey) {
        return settingRepository.findBySettingKey(settingKey);
    }

    public List<Setting> getByCategory(String category) {
        return settingRepository.findByCategoryOrderBySortOrderAsc(category);
    }

    public List<Setting> getEnabledByCategory(String category) {
        return settingRepository.findEnabledByCategoryOrderBySortOrder(category);
    }

    public Setting save(Setting setting) {
        // 检查是否已经存在相同的key
        if (setting.getId() == null) {
            if (settingRepository.existsBySettingKey(setting.getSettingKey())) {
                throw new IllegalArgumentException("Setting key already exists: " + setting.getSettingKey());
            }
        }
        return settingRepository.save(setting);
    }

    public void deleteById(Long id) {
        settingRepository.deleteById(id);
    }

    public List<Setting> getAll() {
        return settingRepository.findAll();
    }

    public Setting update(Setting setting) {
        return settingRepository.save(setting);
    }

    /**
     * 根据键获取设置值
     */
    public String getValueByKey(String settingKey) {
        Optional<Setting> setting = findByKey(settingKey);
        return setting.map(Setting::getSettingValue).orElse(null);
    }

    /**
     * 根据键设置值
     */
    public void setValueByKey(String settingKey, String settingValue) {
        Optional<Setting> existingSetting = findByKey(settingKey);
        if (existingSetting.isPresent()) {
            Setting setting = existingSetting.get();
            setting.setSettingValue(settingValue);
            settingRepository.save(setting);
        } else {
            Setting newSetting = new Setting();
            newSetting.setSettingKey(settingKey);
            newSetting.setSettingValue(settingValue);
            settingRepository.save(newSetting);
        }
    }
}