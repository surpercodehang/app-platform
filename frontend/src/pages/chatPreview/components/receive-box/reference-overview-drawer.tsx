/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import { Drawer } from 'antd';
import { useTranslation } from 'react-i18next';
import './styles/message-detail.scss';

/**
 * 引用总览抽屉
 *
 * @isOpen 显示隐藏
 * @setIsOpen 显示隐藏回调
 * @reference 溯源数据
 * @usedReferences 使用的引用数据
 */
const ReferenceOverviewDrawer = (props: any) => {
  const { t } = useTranslation();
  const { isOpen, setIsOpen, usedReferences } = props;

  // 判断是否为URL
  const isUrl = (str) => {
    try {
      new URL(str);
      return true;
    } catch {
      return false;
    }
  };

  // 关闭抽屉回调
  const onClose = () => {
    setIsOpen(false);
  };

  return (
    <Drawer
      destroyOnClose
      title={
        <div className='reference-overview-title'>
          <span className='reference-overview-title-icon'>📚</span>
          <span>引用总览 ({usedReferences.length} 个引用)</span>
        </div>
      }
      width={800}
      open={isOpen}
      onClose={onClose}
    >
      <div className='reference-overview-content'>
        {usedReferences.length === 0 ? (
          <div className='reference-overview-empty'>
            <span>暂无引用数据</span>
          </div>
        ) : (
          usedReferences.map((ref, index) => {
            const title = ref.metadata?.title || ref.source || '未知来源';
            const sourceUrl = ref.metadata?.url || ref.source;
            const url = sourceUrl && isUrl(sourceUrl) ? sourceUrl : null;
            const txtContent = ref.txt || ref.text || '无文本内容';

            return (
              <div key={index} className='reference-overview-item'>
                <div className='reference-overview-item-number-circle'>
                  {ref.number}
                </div>
                <div className='reference-overview-item-content'>
                  <div className='reference-overview-item-header'>
                    {url ? (
                      <a
                        href={url}
                        target='_blank'
                        rel='noopener noreferrer'
                        className='reference-overview-item-title-link'
                      >
                        {title}
                      </a>
                    ) : (
                      <span className='reference-overview-item-title'>
                        {title}
                      </span>
                    )}
                  </div>
                  <div className='reference-overview-item-text'>
                    {txtContent}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </Drawer>
  );
};

export default ReferenceOverviewDrawer;
