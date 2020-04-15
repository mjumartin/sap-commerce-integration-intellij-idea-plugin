package com.intellij.idea.plugin.hybris.project.utils;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * @author Eugene.Kudelevsky
 */
public class HybrisRootUtil {

    private HybrisRootUtil() {
    }

    @Nullable
    public static VirtualFile findPlatformRootDirectory(@NotNull final Project project) {
        final Module platformModule =
            Arrays.stream(ModuleManager.getInstance(project).getModules())
                  .filter(module -> HybrisModuleDescriptor.getDescriptorType(module) == HybrisModuleDescriptorType.PLATFORM)
                  .findAny()
                  .orElse(null);

        return platformModule == null ? null
            : Arrays.stream(ModuleRootManager.getInstance(platformModule).getContentRoots())
                    .filter(vFile -> vFile.findChild(HybrisConstants.EXTENSIONS_XML) != null)
                    .findAny()
                    .orElse(null);
    }
}
