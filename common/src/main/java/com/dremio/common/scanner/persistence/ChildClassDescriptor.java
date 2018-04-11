/*
 * Copyright (C) 2017-2018 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dremio.common.scanner.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * a class that implements a specific type
 */
public final class ChildClassDescriptor {

  private final String name;
  private final boolean isAbstract;

  @JsonCreator
  public ChildClassDescriptor(
      @JsonProperty("name") String name,
      @JsonProperty("abstract") boolean isAbstract) {
    this.name = name;
    this.isAbstract = isAbstract;
  }

  /**
   * @return the class name
   */
  public String getName() {
    return name;
  }

  /**
   * @return whether the class is abstract
   */
  public boolean isAbstract() {
    return isAbstract;
  }

  @Override
  public String toString() {
    return "ChildClassDescriptor [name=" + name + ", isAbstract=" + isAbstract + "]";
  }

}