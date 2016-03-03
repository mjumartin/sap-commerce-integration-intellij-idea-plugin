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

package com.intellij.idea.plugin.hybris.common.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created 11:23 PM 03 February 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class CollectionUtils {

    @NotNull
    public static <T> Iterable<T> emptyIfNull(@Nullable final Iterable<T> iterable)
    {
        return null == iterable ? Collections.<T>emptyList() : iterable;
    }

    @NotNull
    public static <T> Collection<T> emptyCollectionIfNull(@Nullable final Collection<T> collection)
    {
        return null == collection ? Collections.<T>emptyList() : collection;
    }

    @NotNull
    public static <T> List<T> emptyListIfNull(@Nullable final List<T> list)
    {
        return null == list ? Collections.<T>emptyList() : list;
    }

}
