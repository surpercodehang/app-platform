/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.jober.aipp.northbound;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.jane.common.entity.OperationContext;
import modelengine.fit.jane.common.response.Rsp;
import modelengine.fit.jober.aipp.condition.AppQueryCondition;
import modelengine.fit.jober.aipp.dto.AppBuilderAppMetadataDto;
import modelengine.fit.jober.aipp.dto.chat.AppMetadata;
import modelengine.fit.jober.aipp.dto.chat.AppQueryParams;
import modelengine.fit.jober.aipp.genericable.adapter.AppBuilderAppServiceAdapter;
import modelengine.fit.jober.aipp.service.AppBuilderAppService;
import modelengine.fit.jober.common.RangedResultSet;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.beans.BeanUtils;

import java.util.stream.Collectors;

/**
 * {@link AppBuilderAppService} 的适配器类的实现类。
 *
 * @author 曹嘉美
 * @since 2024-12-19
 */
@Component
public class AppBuilderAppServiceAdapterImpl implements AppBuilderAppServiceAdapter {
    private final AppBuilderAppService appBuilderAppService;

    public AppBuilderAppServiceAdapterImpl(AppBuilderAppService appBuilderAppService) {
        this.appBuilderAppService = notNull(appBuilderAppService, "The app builder app service cannot be null.");
    }

    @Override
    public RangedResultSet<AppMetadata> list(AppQueryParams params, OperationContext context) {
        AppQueryCondition appQueryCondition = BeanUtils.copyProperties(params, AppQueryCondition.class);
        if (params.getType() == null) {
            params.setType("app");
        }
        appQueryCondition.setTenantId(context.getTenantId());
        appQueryCondition.setType(params.getType());
        Rsp<RangedResultSet<AppBuilderAppMetadataDto>> rsp = this.appBuilderAppService.list(appQueryCondition,
                context, params.getOffset(), params.getLimit());
        return this.appMetadataDtoConvertToAdapter(rsp.getData());
    }

    RangedResultSet<AppMetadata> appMetadataDtoConvertToAdapter(RangedResultSet<AppBuilderAppMetadataDto> dto) {
        return RangedResultSet.create(dto.getResults()
                .stream()
                .map(appBuilderAppMetadataDto -> BeanUtils.copyProperties(appBuilderAppMetadataDto, AppMetadata.class))
                .collect(Collectors.toList()), dto.getRange());
    }
}