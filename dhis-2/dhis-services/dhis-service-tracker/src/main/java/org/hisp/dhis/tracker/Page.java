/*
 * Copyright (c) 2004-2023, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.tracker;

import java.util.List;
import java.util.function.Function;
import java.util.function.LongSupplier;
import javax.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Create a page of items. A page is guaranteed to have items, a page number and page size. All
 * other fields are optional.
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Page<T> {
  @Nonnull private final List<T> items;
  private final int page;
  private final int pageSize;
  private final Long total;
  private final Integer prevPage;
  private final Integer nextPage;

  public static <T> Page<T> empty() {
    return new Page<>(List.of(), 0, 0, 0L, null, null);
  }

  public Page(@Nonnull List<T> items, @Nonnull PageParams pageParams, @Nonnull LongSupplier total) {
    this.page = pageParams.getPage();
    this.pageSize = pageParams.getPageSize();

    if (pageParams.isPageTotal()) {
      this.total = total.getAsLong();
    } else {
      this.total = null;
    }

    this.prevPage = pageParams.getPage() > 1 ? pageParams.getPage() - 1 : null;
    if (items.size() > pageParams.getPageSize()) {
      this.items = items.subList(0, pageParams.getPageSize());
      this.nextPage = pageParams.getPage() + 1;
    } else {
      this.items = items;
      this.nextPage = null;
    }
  }

  /**
   * Create a new page based on an existing one but with given {@code items}. Page related counts
   * will not be changed so make sure the given {@code items} match the previous page size.
   */
  public <U> Page<U> withItems(List<U> items) {
    return new Page<>(items, this.page, this.pageSize, this.total, this.prevPage, this.nextPage);
  }

  public <R> Page<R> withItems(Function<T, R> map) {
    return new Page<>(
        items.stream().map(map).toList(),
        this.page,
        this.pageSize,
        this.total,
        this.prevPage,
        this.nextPage);
  }

  public static <T> Page<T> withTotals(List<T> items, int page, int pageSize, long total) {
    return new Page<>(items, page, pageSize, total, null, null);
  }

  public static <T> Page<T> withoutTotals(List<T> items, int page, int pageSize) {
    return new Page<>(items, page, pageSize, null, null, null);
  }

  public static <T> Page<T> withPrevAndNext(
      List<T> items, int page, int pageSize, Integer prevPage, Integer nextPage) {
    return new Page<>(items, page, pageSize, null, prevPage, nextPage);
  }
}
