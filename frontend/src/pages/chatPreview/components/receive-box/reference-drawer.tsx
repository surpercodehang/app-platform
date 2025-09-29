/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, { useEffect, useState } from 'react';
import { Drawer } from 'antd';
import { useTranslation } from 'react-i18next';
import 'highlight.js/styles/monokai-sublime.min.css';
import './styles/message-detail.scss';

/**
 * 溯源抽屉弹窗
 *
 * @isOpen 显示隐藏
 * @setIsOpen 显示隐藏回调
 * @reference 溯源数据
 * @referenceStr 溯源拼接后数据
 * @referenceIndex 溯源对应的索引
 */
const MessageRefence = (props: any) => {
  const { t } = useTranslation();
  const { isOpen, setIsOpen, reference, referenceStr, referenceIndex } = props;
  const [text, setText] = useState([]);

  // 在 MessageRefence 组件的 useEffect 中
  useEffect(() => {
    if (isOpen && referenceStr) {
      let referenceList = reference[referenceIndex] || [];
      const allRefKeys = Object.keys(referenceList);

      // 收集所有使用的引用键（按出现顺序） - 与MessageBox组件相同的逻辑
      const usedRefKeysInOrder = [];
      const tempUsedKeys = new Set();

      if (props.content) {
        const refMatches = props.content.match(/<ref>(.*?)<\/ref>/g) || [];
        refMatches.forEach(match => {
          const keyContent = match.replace(/<ref>|<\/ref>/g, '');
          const keys = keyContent.split('_');
          keys.forEach(key => {
            if (allRefKeys.includes(key) && !tempUsedKeys.has(key)) {
              tempUsedKeys.add(key);
              usedRefKeysInOrder.push(key);
            }
          });
        });
      }

      // 建立新编号到引用键的映射
      const newNumberToRefKey = new Map();
      usedRefKeysInOrder.forEach((key, index) => {
        newNumberToRefKey.set(index + 1, key);
      });

      // 如果 referenceStr 是单个数字（新编号）
      if (!isNaN(referenceStr)) {
        const newNumber = parseInt(referenceStr);
        if (newNumber > 0 && newNumber <= usedRefKeysInOrder.length) {
          const refKey = newNumberToRefKey.get(newNumber);
          setText([referenceList[refKey]]);
        }
      }
      // 保持对旧格式的兼容
      else {
        let obj = {};
        Object.keys(referenceList).forEach((item) => {
          obj[item] = referenceList[item];
        });
        const arr = referenceStr.split('_')
          .filter(item => item)
          .map((item) => obj[item]);
        setText(arr);
      }
    }
  }, [isOpen, referenceStr, reference, referenceIndex, props.content]);

  // 关闭抽屉回调
  const onClose = () => {
    setIsOpen(false);
    document.querySelectorAll('.reference-circle').forEach((item) => {
      item.classList.remove('reference-circle-active');
    });
  };

  // 判断是否为URL
  const isUrl = (str) => {
    try {
      new URL(str);
      return true;
    } catch {
      return false;
    }
  };

  return (
    <Drawer destroyOnClose title={t('source')} width={800} open={isOpen} onClose={onClose}>
      {text.map((item, index) => {
        const sourceText = item?.source || item?.metadata?.url || '';
        const txtContent = item?.txt || item?.text || item || '';
        const title = item?.metadata?.title || sourceText || '未知来源';
        const isSourceUrl = isUrl(sourceText);

        return (
          <div key={index} className='reference-item'>
            <div className='reference-content'>
              <span className='reference-text'>{txtContent}</span>
            </div>
            {sourceText && (
              <div className='reference-source'>
                <div className='reference-source-header'>
                  {/* 移除编号显示 */}
                  <span className='reference-source-label'>来源：</span>
                </div>
                {isSourceUrl ? (
                  <a
                    href={sourceText}
                    target='_blank'
                    rel='noopener noreferrer'
                    className='reference-url'
                  >
                    {title}
                  </a>
                ) : (
                  <span className='reference-doc-name'>{title}</span>
                )}
              </div>
            )}
          </div>
        );
      })}
    </Drawer>
  );
};

export default MessageRefence;
