/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, { useEffect, useState } from 'react';
import { markedProcess } from '../../utils/marked-process';
import { useTranslation } from 'react-i18next';
import { Message } from '@/shared/utils/message';
import { isChatRunning } from '@/shared/utils/chat';
import { useAppSelector } from '@/store/hook';
import Feedbacks from './feedbacks';
import PictureList from './picture-list';
import ThinkBlock from './think-block';
import StepBlock from './step-block';
import ReferenceOverviewDrawer from './reference-overview-drawer';
import 'highlight.js/styles/monokai-sublime.min.css';
import './styles/message-detail.scss';
import store from '@/store/store';
import {setCurrentAnswer} from "@/store/chatStore/chatStore";
import {useMemo } from 'react';

/**
 * 消息详情
 * @return {JSX.Element}
 * @content 消息内容
 * @instanceId 租户ID
 * @feedbackStatus 点赞点踩状态
 * @reference 溯源返回reference列表
 * @msgType 溯源返回状态
 * @pictureList 图片列表
 */
const MessageBox = (props: any) => {
  const { content, thinkTime, instanceId, finished, feedbackStatus, status, reference, msgType, pictureList } = props;
  const { t } = useTranslation();
  const [thinkContent, setThinkContent] = useState('');
  const [answerContent, setAnswerContent] = useState('');
  const [stepContent, setStepContent] = useState('');
  const [showStep, setShowStep] = useState(false);
  const [replacedText, setReplacedText] = useState<any>(null);
  const [hoveredReference, setHoveredReference] = useState<{title: string, summary: string} | null>(null);
  const [tooltipPosition, setTooltipPosition] = useState({ x: 0, y: 0 });
  const [showReferenceOverview, setShowReferenceOverview] = useState(false);
  const chatReference = useAppSelector((state) => state.chatCommonStore.chatReference);
  const referenceList = useAppSelector((state) => state.chatCommonStore.referenceList);
  
  // 计算实际引用数量
  const getReferenceCount = () => {
    if (!reference || !Array.isArray(reference)) return 0;
    let count = 0;
    reference.forEach((refGroup) => {
      if (refGroup && typeof refGroup === 'object') {
        count += Object.keys(refGroup).length;
      }
    });
    return count;
  };

  // 计算当前消息实际使用的引用数量
  const getUsedReferenceCount = () => {
    if (!reference || !Array.isArray(reference) || reference.length === 0) return 0;

    const referenceList = reference[0] || {};
    const allRefKeys = Object.keys(referenceList);

    const usedKeys = new Set();

    // 从内容中提取使用的引用键
    if (content) {
      const refMatches = content.match(/<ref>(.*?)<\/ref>/g) || [];
      refMatches.forEach((match: string) => {
        const keyContent = match.replace(/<ref>|<\/ref>/g, '');
        const keys = keyContent.split('_');
        keys.forEach((key: string) => {
          if (allRefKeys.includes(key)) {
            usedKeys.add(key);
          }
        });
      });
    }

    return usedKeys.size;
  };

  // 获取当前消息使用的所有引用数据（按新编号排序）
  const getUsedReferences = () => {
    if (!reference || !Array.isArray(reference) || reference.length === 0) return [];

    const referenceList = reference[0] || {};
    const allRefKeys = Object.keys(referenceList);

    // 收集所有使用的引用键（按出现顺序）
    const usedRefKeysInOrder: string[] = [];
    const tempUsedKeys = new Set<string>();

    // 从内容中提取使用的引用键（保持出现顺序）
    if (content) {
      const refMatches = content.match(/<ref>(.*?)<\/ref>/g) || [];
      refMatches.forEach((match: string) => {
        const keyContent = match.replace(/<ref>|<\/ref>/g, '');
        const keys = keyContent.split('_');
        keys.forEach((key: string) => {
          if (allRefKeys.includes(key) && !tempUsedKeys.has(key)) {
            tempUsedKeys.add(key);
            usedRefKeysInOrder.push(key);
          }
        });
      });
    }

    // 按出现顺序创建引用数据（重新编号从1开始）
    const usedRefs = usedRefKeysInOrder.map((key, index) => ({
      id: key,
      number: index + 1, // 重新编号：1,2,3,4,5,6
      data: referenceList[key] // 将引用数据放在 data 字段中
    }));

    return usedRefs;
  };

  const usedReferences = useMemo(() => {
    return getUsedReferences();
  }, [reference, content]); // 当 reference 或 content 变化时重新计算

  const usedReferenceCount = useMemo(() => {
    return getUsedReferenceCount();
  }, [reference, content]);

  // 判断是否为URL
  const isUrl = (str: string) => {
    try {
      new URL(str);
      return true;
    } catch {
      return false;
    }
  };

  // 获取引用数据
  const getReferenceData = (refNumber: number) => {
    if (!reference || !Array.isArray(reference) || reference.length === 0) return null;
    
    const referenceList = reference[0] || {};
    const allRefKeys = Object.keys(referenceList);
    
    // 收集所有使用的引用键（按出现顺序）
    const usedRefKeysInOrder: string[] = [];
    const tempUsedKeys = new Set<string>();
    
    if (content) {
      const refMatches = content.match(/<ref>(.*?)<\/ref>/g) || [];
      refMatches.forEach((match: string) => {
        const keyContent = match.replace(/<ref>|<\/ref>/g, '');
        const keys = keyContent.split('_');
        keys.forEach((key: string) => {
          if (allRefKeys.includes(key) && !tempUsedKeys.has(key)) {
            tempUsedKeys.add(key);
            usedRefKeysInOrder.push(key);
          }
        });
      });
    }
    
    // 根据编号获取对应的引用数据
    if (refNumber > 0 && refNumber <= usedRefKeysInOrder.length) {
      const refKey = usedRefKeysInOrder[refNumber - 1];
      const refData = referenceList[refKey];
      return refData;
    }
    return null;
  };

  // 在 MessageBox 组件中修改 regExpReplace 函数
  // 新的正则替换逻辑 - 按使用顺序重新编号，支持悬停和点击
  const regExpReplace = (content: string, index: any) => {
    let strs = content;
    let replacedStrs = strs.replace(/<\/ref><ref>/g, '_');

    // 获取可用的引用键
    const referenceListData = Array.isArray(reference) ? (reference[0] || {}) : (referenceList || {});
    const allRefKeys = Object.keys(referenceListData);

    // 收集所有使用的引用键（按出现顺序）
    const usedRefKeysInOrder: string[] = [];
    const tempUsedKeys = new Set<string>();

    // 第一次遍历：收集所有使用的引用键（保持出现顺序）
    replacedStrs.replace(/<ref>(.*?)<\/ref>/g, (match: string, key: string) => {
      const splitStr = key.split('_');
      splitStr.forEach((refKey: string) => {
        if (allRefKeys.includes(refKey) && !tempUsedKeys.has(refKey)) {
          tempUsedKeys.add(refKey);
          usedRefKeysInOrder.push(refKey);
        }
      });
      return '';
    });

    // 建立使用引用键到新编号的映射（从1开始重新编号）
    const refKeyToNewNumber = new Map<string, number>();
    usedRefKeysInOrder.forEach((key: string, index: number) => {
      refKeyToNewNumber.set(key, index + 1);
    });

    // 第二次遍历：替换为重新编号的圆形数字，支持悬停和点击
    const replacedStr = replacedStrs.replace(/<ref>(.*?)<\/ref>/g, (match: string, key: string) => {
      const splitStr = key.split('_');
      const validRefs = splitStr.filter((item: string) => allRefKeys.includes(item));

      if (validRefs.length > 0) {
        // 获取对应的新编号并排序
        const refNumbers = validRefs.map((refKey: string) => refKeyToNewNumber.get(refKey)).filter((num): num is number => num !== undefined).sort((a: number, b: number) => a - b);

        // 为每个数字创建独立的圆形元素，支持悬停和点击
        const circleElements = refNumbers.map((num: number) => {
          // 使用与引用总览相同的数据获取逻辑
          const usedRefs = getUsedReferences();
          const refItem = usedRefs.find(ref => ref.number === num);
          const refData = refItem?.data;
          
          if (!refData) {
            return `<span class="reference-circle">${num}</span>`;
          }
          
          const sourceText = refData?.source || refData?.metadata?.url || '';
          const txtContent = refData?.txt || refData?.text || refData || '';
          const title = refData?.metadata?.title || sourceText || '未知来源';
          const sourceUrl = refData?.metadata?.url || refData?.source;
          const url = sourceUrl && isUrl(sourceUrl) ? sourceUrl : null;
          
          // 转义HTML属性值
          const escapedTitle = title.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
          const escapedSummary = txtContent.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
          const escapedUrl = (url || '').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
          
          return `<span 
            data-reference="${index}-${num}" 
            data-title="${escapedTitle}" 
            data-url="${escapedUrl}" 
            data-summary="${escapedSummary}" 
            class="reference-circle" 
            title="${escapedTitle}"
          >${num}</span>`;
        }).join('');

        return circleElements;
      }
      return '';
    });

    return replacedStr;
  };

  // 拼接content与reference
  const replaceInfo = (content: string, type = '') => {
    let metaContent = [content];
    let mataStr = metaContent.map((item: string, index: number) => {
      return regExpReplace(item, index);
    });
    type ? setThinkContent(mataStr.join('')) : setReplacedText(mataStr.join(''));
  };

  // 点击引用数字的回调 - 直接跳转到URL
  const onClickReference = (e: any) => {
    if (e.target.classList.contains('reference-circle')) {
      if (isChatRunning()) {
        Message({ type: 'warning', content: t('tryLater') });
        return;
      }
      
      const url = e.target.dataset.url;
      if (url) {
        window.open(url, '_blank');
      } else {
        Message({ type: 'info', content: '该引用没有可访问的链接' });
      }
    }
  };

  // 悬停引用数字的回调
  const onMouseEnterReference = (e: any) => {
    if (e.target.classList.contains('reference-circle')) {
      const title = e.target.dataset.title;
      const summary = e.target.dataset.summary;
      const rect = e.target.getBoundingClientRect();
      setTooltipPosition({
        x: rect.left + rect.width / 2,
        y: rect.top - 10
      });
      setHoveredReference({ title, summary });
    }
  };

  // 离开引用数字的回调
  const onMouseLeaveReference = (e: any) => {
    if (e.target.classList.contains('reference-circle')) {
      setHoveredReference(null);
    }
  };

  // 处理鼠标移动事件，用于更新悬停位置
  const onMouseMoveReference = (e: any) => {
    if (e.target.classList.contains('reference-circle')) {
      const rect = e.target.getBoundingClientRect();
      setTooltipPosition({
        x: rect.left + rect.width / 2,
        y: rect.top - 10
      });
    }
  };

  // 设置接受消息显示内容
  const getMessageContent = () => {
    if (pictureList) {
      return <PictureList pictureList={pictureList}></PictureList>;
    } else {
      return (
        <div
          className='receive-info-html'
          dangerouslySetInnerHTML={{ __html: markedProcess(replacedText) }}
        ></div>
      );
    }
  };

  // a标签点击（打开新窗口跳转）
  const recieveClick = (event: any) => {
    if (event.target && event.target.nodeName.toLowerCase() === 'a') {
      event.preventDefault();
      window.open(event.target.href, '_blank');
    }
  }

  // 智能体调度工具内容处理
  const getAgentOutput = (str: string) => {
    let lastOpenTag: any = null;
    let hasStepContent = false;
    let tagMap: any = {
      reasoning: t('thinking'),
      step: {
        name: t('steps'),
        index: 1
      },
      tool: t('toolResult')
    }
    tagMap.step.index = 1;
    let output = str.replace(/<(\/?)(reasoning|step|tool|final)>/g, (match: string, isClose: string, tag: string) => {
      if (match && !['<final>', '</final>'].includes(match)) {
        setShowStep(true);
        hasStepContent = true;
      }
      if (isClose) {
        if (tag === lastOpenTag) lastOpenTag = null;
        return '</div>';
      } else {
        lastOpenTag = tag;
        let tagTitle = ''
        if (tag === 'step') {
          tagTitle = tagMap[tag] ? `${tagMap[tag]['name']} ${tagMap.step.index}` : '';
          tagMap.step.index += 1;
        } else {
          tagTitle = tagMap[tag] || '';
        }
        return `${ tagTitle ? `<div class="${tag}"><span>${tagTitle}</span>` : `<div class="${tag}">` }`;
      }
    });
    if (lastOpenTag) {
      output += '</div>';
    };
    if (!hasStepContent) {
      return str
    }
    return setClosureLabel(output);
  }

  // 智能体调度工具结束标签处理
  const setClosureLabel = (str: string) => {
    const regex = /<div class="final">([\s\S]*?)<\/div>/;
    const match = str.match(regex);
    setStepContent(str.replace(regex, ''));
    if (match && match[1]) {
      return match[1].trim();
    } else {
      return '';
    }
  }

  useEffect(() => {
    const finalContent = getAgentOutput(answerContent);
    if (msgType === 'META_MSG' || chatReference) {
      replaceInfo(finalContent);
    } else {
      setReplacedText(finalContent);
    }
  }, [answerContent]);


  useEffect(() => {
    const thinkStartIdx = content.indexOf('<think>');
    let thinkEndIdx = content.indexOf('</think>');
    if (thinkStartIdx > -1 && thinkEndIdx < 0) {
      thinkEndIdx = content.length - '</think>'.length;
    }
    if (thinkEndIdx > 0) {
      thinkEndIdx = thinkEndIdx + '</think>'.length;
    }
    if (thinkStartIdx > -1) {
      const thinkContent = content.slice(thinkStartIdx, thinkEndIdx);
      if (msgType === 'META_MSG' || chatReference) {
        replaceInfo(thinkContent, 'deepseek');
      } else {
        setThinkContent(thinkContent);
      }
      setAnswerContent(content.slice(thinkEndIdx));
    } else {
      setAnswerContent(content);
    }
  }, [content]);

  // 接受消息点击事件
  useEffect(() => {
    const container = document.querySelector('.message-box');
    
    if (container) {
      container.addEventListener('click', recieveClick);
    }
    
    return () => {
      if (container) {
        container.removeEventListener('click', recieveClick);
      }
    }
  }, []);

  useEffect(() => {
    store.dispatch(setCurrentAnswer(replacedText));
    
    // 在内容更新后，重新绑定引用数字的事件监听器
    if (replacedText) {
      // 延迟一点时间确保DOM已经更新
      setTimeout(() => {
        const referenceCircles = document.querySelectorAll('.reference-circle');
        
        referenceCircles.forEach((circle: any) => {
          // 移除旧的事件监听器
          circle.removeEventListener('click', handleReferenceClick);
          circle.removeEventListener('mouseenter', handleReferenceMouseEnter);
          circle.removeEventListener('mouseleave', handleReferenceMouseLeave);
          circle.removeEventListener('mousemove', handleReferenceMouseMove);
          
          // 添加新的事件监听器
          circle.addEventListener('click', handleReferenceClick);
          circle.addEventListener('mouseenter', handleReferenceMouseEnter);
          circle.addEventListener('mouseleave', handleReferenceMouseLeave);
          circle.addEventListener('mousemove', handleReferenceMouseMove);
        });
      }, 100);
    }
  }, [replacedText]);

  // 定义引用数字的事件处理函数
  const handleReferenceClick = (e: any) => {
    e.stopPropagation();
    if (isChatRunning()) {
      Message({ type: 'warning', content: t('tryLater') });
      return;
    }
    
    const url = e.target.dataset.url;
    if (url) {
      window.open(url, '_blank');
    } else {
      Message({ type: 'info', content: '该引用没有可访问的链接' });
    }
  };

  const handleReferenceMouseEnter = (e: any) => {
    const title = e.target.dataset.title;
    const summary = e.target.dataset.summary;
    const rect = e.target.getBoundingClientRect();
    setTooltipPosition({
      x: rect.left + rect.width / 2,
      y: rect.top - 10
    });
    setHoveredReference({ title, summary });
  };

  const handleReferenceMouseLeave = (e: any) => {
    setHoveredReference(null);
  };

  const handleReferenceMouseMove = (e: any) => {
    const rect = e.target.getBoundingClientRect();
    setTooltipPosition({
      x: rect.left + rect.width / 2,
      y: rect.top - 10
    });
  };

  // 在 MessageBox 组件的返回部分修改
  return (
    <>
      <div className='receive-info'>
        {(thinkContent && status !== 'TERMINATED') && <ThinkBlock content={thinkContent} thinkTime={thinkTime} />}
        {(showStep && status !== 'TERMINATED' ) && <StepBlock content={stepContent} finished={finished} />}
        {getMessageContent()}
        { finished &&
        <div className='feed-footer'>
          <Feedbacks
            instanceId={instanceId}
            feedbackStatus={feedbackStatus}
            refreshFeedbackStatus={props.refreshFeedbackStatus}
          />
        </div> }

        {/* 引用总览按钮 - 使用缓存的引用数量 */}
        {reference?.length > 0 && (
          <div className='reference-overview-section'>
            <button
              className='reference-overview-btn'
              onClick={() => setShowReferenceOverview(true)}
            >
              <span className='reference-overview-icon'>📚</span>
              <span className='reference-overview-text'>
                查看引用 ({usedReferenceCount} 个引用)
              </span>
            </button>
          </div>
        )}

        {/* 引用总览抽屉 - 传递缓存的引用数据 */}
        {reference?.length > 0 && (
          <ReferenceOverviewDrawer
            isOpen={showReferenceOverview}
            setIsOpen={setShowReferenceOverview}
            usedReferences={usedReferences}
          />
        )}

        {/* 悬停显示引用信息 */}
        {hoveredReference && (
          <div 
            className='reference-hover-tooltip'
            style={{
              left: `${tooltipPosition.x}px`,
              top: `${tooltipPosition.y}px`,
              transform: 'translateX(-50%)'
            }}
          >
            <div className='reference-hover-title'>{hoveredReference.title}</div>
            <div className='reference-hover-summary'>{hoveredReference.summary}</div>
          </div>
        )}
      </div>
    </>
  );
};

export default MessageBox;
