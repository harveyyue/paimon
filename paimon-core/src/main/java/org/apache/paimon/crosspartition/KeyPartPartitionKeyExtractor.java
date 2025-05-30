/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.crosspartition;

import org.apache.paimon.codegen.CodeGenUtils;
import org.apache.paimon.codegen.Projection;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.data.InternalRow;
import org.apache.paimon.schema.TableSchema;
import org.apache.paimon.table.sink.PartitionKeyExtractor;
import org.apache.paimon.types.RowType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** A {@link PartitionKeyExtractor} to {@link InternalRow} with only key and partition fields. */
public class KeyPartPartitionKeyExtractor implements PartitionKeyExtractor<InternalRow> {

    private final Projection partitionProjection;
    private final Projection keyProjection;

    public KeyPartPartitionKeyExtractor(TableSchema schema) {
        List<String> partitionKeys = schema.partitionKeys();
        RowType keyPartType =
                schema.projectedLogicalRowType(
                        Stream.concat(schema.trimmedPrimaryKeys().stream(), partitionKeys.stream())
                                .collect(Collectors.toList()));
        this.partitionProjection = CodeGenUtils.newProjection(keyPartType, partitionKeys);
        this.keyProjection = CodeGenUtils.newProjection(keyPartType, schema.primaryKeys());
    }

    @Override
    public BinaryRow partition(InternalRow record) {
        return partitionProjection.apply(record);
    }

    @Override
    public BinaryRow trimmedPrimaryKey(InternalRow record) {
        return keyProjection.apply(record);
    }
}
