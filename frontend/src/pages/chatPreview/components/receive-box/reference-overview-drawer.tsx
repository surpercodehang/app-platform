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
 * @usedReferences 使用的引用数据
 */
const ReferenceOverviewDrawer = (props: any) => {
  const { t } = useTranslation();
  const { isOpen, setIsOpen, usedReferences } = props;

  // 判断是否为URL
  const isUrl = (str: string) => {
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

  // 点击引用项的回调
  const onClickReference = (ref: any) => {
    const sourceText = ref.data?.source || ref.data?.metadata?.url || '';
    const sourceUrl = ref.data?.metadata?.url || ref.data?.source;
    const url = sourceUrl && isUrl(sourceUrl) ? sourceUrl : null;
    
    if (url) {
      window.open(url, '_blank');
    } else {
      // 如果没有URL，可以显示提示或者不做任何操作
      console.log('该引用没有可访问的链接');
    }
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
          usedReferences.map((ref: any) => {
            const item = ref.data; // 从 data 字段获取引用数据
            const sourceText = item?.source || item?.metadata?.url || '';
            const txtContent = item?.txt || item?.text || item || '';
            const title = item?.metadata?.title || sourceText || '未知来源';
            const sourceUrl = item?.metadata?.url || item?.source;
            const url = sourceUrl && isUrl(sourceUrl) ? sourceUrl : null;

            return (
              <div 
                key={ref.id} 
                className='reference-overview-item'
                onClick={() => onClickReference(ref)}
                style={{ cursor: 'pointer' }}
              >
                <div className='reference-overview-item-number-circle'>
                  {ref.number}
                </div>
                <div className='reference-overview-item-content'>
                  <div className='reference-overview-item-header'>
                    <span className='reference-overview-item-title'>
                      {title}
                    </span>
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
