/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.type.system.meta.impl;

import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class CaseInsensitive {

    @NotNull
    public static String eraseCase(final @NotNull String key) {
        return key.toLowerCase();
    }

    public static class NoCaseMap<V> {

        private final Map<String, V> myMap = new HashMap<>();

        public void put(final @NotNull String key, final @Nullable V value) {
            myMap.put(eraseCase(key), value);
        }

        public void putAll(@NotNull final NoCaseMap<V> map) {
            myMap.putAll(map.myMap);
        }

        @Nullable
        public V get(final @NotNull String key) {
            return myMap.get(eraseCase(key));
        }

        @NotNull
        public Collection<V> values() {
            return myMap.values();
        }
    }

    public static class NoCaseMultiMap<V> {

        private final MultiMap<String, V> myMultiMap = MultiMap.createLinked();

        public void putValue(final @NotNull String key, final @NotNull V value) {
            myMultiMap.putValue(eraseCase(key), value);
        }

        public void putAllValues(@NotNull final NoCaseMultiMap<V> map) {
            myMultiMap.putAllValues(map.myMultiMap);
        }

        @NotNull
        public Collection<? extends V> values() {
            return myMultiMap.values();
        }

        @NotNull
        public Collection<? extends V> get(final @NotNull String key) {
            return myMultiMap.get(eraseCase(key));
        }

    }


}
