/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import { Modal } from 'antd';
import { useTranslation } from 'react-i18next';
import './styles/message-detail.scss';

/**
 * 引用总览模态框
 *
 * @isOpen 显示隐藏
 * @setIsOpen 显示隐藏回调
 * @reference 溯源数据
 */
const ReferenceOverviewModal = (props: any) => {
  const { t } = useTranslation();
  const { isOpen, setIsOpen, reference } = props;

  // 判断是否为URL
  const isUrl = (str) => {
    try {
      new URL(str);
      return true;
    } catch {
      return false;
    }
  };

  // 获取所有引用数据
  const getAllReferences = () => {
    const allRefs = [];
    if (Array.isArray(reference)) {
      reference.forEach((refGroup, groupIndex) => {
        if (refGroup && typeof refGroup === 'object') {
          Object.keys(refGroup).forEach((key) => {
            const refItem = refGroup[key];
            if (refItem && (refItem.txt || refItem.text || refItem.source)) {
              allRefs.push({
                id: `${groupIndex}-${key}`,
                groupIndex,
                key,
                ...refItem
              });
            }
          });
        }
      });
    }
    return allRefs;
  };

  const allReferences = getAllReferences();

  // 关闭模态框回调
  const onClose = () => {
    setIsOpen(false);
  };

  return (
    <Modal
      title={
        <div className='reference-overview-title'>
          <span className='reference-overview-title-icon'>📚</span>
          <span>引用总览 ({allReferences.length} 个引用)</span>
        </div>
      }
      open={isOpen}
      onCancel={onClose}
      footer={null}
      width={900}
      className='reference-overview-modal'
    >
      <div className='reference-overview-content'>
        {allReferences.length === 0 ? (
          <div className='reference-overview-empty'>
            <span>暂无引用数据</span>
          </div>
        ) : (
          allReferences.map((ref, index) => {
            const title = ref.metadata?.title || ref.source || '未知来源';
            // 优先使用metadata.url，然后使用source字段
            const sourceUrl = ref.metadata?.url || ref.source;
            const url = sourceUrl && isUrl(sourceUrl) ? sourceUrl : null;
            const txtContent = ref.txt || ref.text || '无文本内容';
            
            return (
              <div key={index} className='reference-overview-item'>
                <div className='reference-overview-item-number'>
                  [{index + 1}]
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
    </Modal>
  );
};

export default ReferenceOverviewModal;
