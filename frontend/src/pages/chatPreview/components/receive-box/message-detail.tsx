/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, { useEffect, useState, useRef } from 'react';
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
import { Tooltip } from 'antd';
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
  const [replacedNodes, setReplacedNodes] = useState<React.ReactNode>(null);
  const [showReferenceOverview, setShowReferenceOverview] = useState(false);
  const chatReference = useAppSelector((state) => state.chatCommonStore.chatReference);
  const referenceList = useAppSelector((state) => state.chatCommonStore.referenceList);
  const contentContainerRef = useRef<HTMLDivElement>(null);
  
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

  /**
   * 渲染正文 + 引用 - 保持 Markdown 格式完整性
   */
  const renderWithReferences = (rawContent: string) => {
    if (!rawContent) return null;

    // 合并相邻的引用标签
    let processedContent = rawContent.replace(/<\/ref><ref>/g, '_');
    
    // 构建引用键到新编号的映射
    const refKeyToNewNumber = new Map<string, number>();
    usedReferences.forEach(ref => {
      refKeyToNewNumber.set(ref.id, ref.number);
    });
    
    // 收集引用数据用于后续替换
    const refPlaceholders: Array<{id: string, refData: any[]}> = [];
    let placeholderIndex = 0;
    
    processedContent = processedContent.replace(/<ref>(.*?)<\/ref>/g, (match, keyContent) => {
      const keys = keyContent.split('_').filter((k: string) => refKeyToNewNumber.has(k));
      const refNumbers = keys.map((k: string) => refKeyToNewNumber.get(k)!).sort((a: number, b: number) => a - b);
      
      const refData = refNumbers.map((num: number) => {
        const ref = usedReferences.find(r => r.number === num);
        return {
          number: num,
          title: ref?.data?.metadata?.title || ref?.data?.source || '未知来源',
          summary: ref?.data?.txt || ref?.data?.text || '无摘要',
          url: ref?.data?.metadata?.url || ref?.data?.source
        };
      });
      
      // 使用特殊标记作为占位符
      const placeholderId = `REFPLACEHOLDER${placeholderIndex}ENDREF`;
      refPlaceholders.push({ id: placeholderId, refData });
      placeholderIndex++;
      return placeholderId;
    });

    console.log('[Debug 1] Before markdown processing:', processedContent.substring(0, 200));
    console.log('[Debug 1] Placeholders created:', refPlaceholders.length);

    // 使用 marked 处理 markdown（保留完整格式）
    let htmlContent = markedProcess(processedContent);

    console.log('[Debug 2] After markdown processing:', htmlContent.substring(0, 300));
    console.log('[Debug 2] HTML still contains placeholder:', htmlContent.includes('REFPLACEHOLDER'));

    // 将占位符替换为可点击的引用标签
    refPlaceholders.forEach(({ id, refData }) => {
      const refHtml = refData.map((data: any) => {
        // 更安全的转义函数 - 但是不要转义已经存在的实体
        const escapeAttribute = (str: string) => {
          return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
        };
        
        const escapedTitle = escapeAttribute(data.title);
        const escapedSummary = escapeAttribute(data.summary);
        const escapedUrl = escapeAttribute(data.url || '');
        
        return `<span class="reference-circle" data-ref-number="${data.number}" data-ref-url="${escapedUrl}" data-ref-title="${escapedTitle}" data-ref-summary="${escapedSummary}">${data.number}</span>`;
      }).join('');
      
      console.log('[Debug 3] Replacing placeholder:', id, 'with HTML:', refHtml);
      
      // 替换占位符 - 注意可能被 markdown 包裹或转义
      // 尝试多种可能的格式
      const patterns = [
        id,  // 原始格式
        `<p>${id}</p>`,  // 被包裹在 p 标签中
        `>${id}<`,  // 在标签之间
      ];
      
      patterns.forEach(pattern => {
        if (htmlContent.includes(pattern)) {
          // 如果是被 p 标签包裹的，替换整个 p 标签
          if (pattern.startsWith('<p>')) {
            htmlContent = htmlContent.replace(pattern, refHtml);
          } else {
            htmlContent = htmlContent.replace(new RegExp(pattern, 'g'), refHtml);
          }
        }
      });
    });

    console.log('[Debug 4] Final HTML contains references:', htmlContent.includes('reference-circle'));
    console.log('[Debug 4] Final HTML sample:', htmlContent.substring(0, 400));

    return <div dangerouslySetInnerHTML={{ __html: htmlContent }} />;
  };


  // 设置接受消息显示内容
  const getMessageContent = () => {
    if (pictureList) {
      return <PictureList pictureList={pictureList}></PictureList>;
    } else {
      return (
        <div className='receive-info-html' ref={contentContainerRef}>
          {replacedNodes}
        </div>
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
      setReplacedNodes(renderWithReferences(finalContent));
    } else {
      setReplacedNodes(<span dangerouslySetInnerHTML={{ __html: finalContent }} />);
    }
  }, [answerContent, usedReferences]);


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
      setThinkContent(thinkContent);
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

  // 处理引用标签的点击和悬停事件
  useEffect(() => {
    const container = contentContainerRef.current;
    if (!container) return;

    const handleReferenceClick = (event: Event) => {
      const target = event.target as HTMLElement;
      if (target.classList.contains('reference-circle')) {
        event.preventDefault();
        event.stopPropagation();
        
        const url = target.getAttribute('data-ref-url');
        
        if (isChatRunning()) {
          Message({ type: 'warning', content: t('tryLater') });
          return;
        }
        
        if (url && /^https?:\/\//.test(url)) {
          window.open(url, '_blank');
        } else {
          Message({ type: 'info', content: '该引用没有可访问的链接' });
        }
      }
    };

    const handleReferenceMouseEnter = (event: Event) => {
      const target = event.target as HTMLElement;
      if (target.classList.contains('reference-circle')) {
        // 清除之前可能存在的 tooltip
        const existingTooltips = document.querySelectorAll('[data-tooltip-id="ref-tooltip"]');
        existingTooltips.forEach(t => t.remove());
        
        const title = target.getAttribute('data-ref-title') || '未知来源';
        const summary = target.getAttribute('data-ref-summary') || '无摘要';
        
        const tooltip = document.createElement('div');
        tooltip.className = 'reference-hover-tooltip';
        tooltip.setAttribute('data-tooltip-id', 'ref-tooltip');
        tooltip.innerHTML = `
          <div class="reference-hover-title">${title}</div>
          <div class="reference-hover-summary">${summary}</div>
        `;
        
        // 计算 tooltip 位置
        const rect = target.getBoundingClientRect();
        tooltip.style.left = `${rect.left}px`;
        tooltip.style.top = `${rect.bottom + 8}px`;
        
        document.body.appendChild(tooltip);
        
        // 移除 tooltip
        const removeTooltip = () => {
          const existingTooltip = document.querySelector('[data-tooltip-id="ref-tooltip"]');
          if (existingTooltip) {
            existingTooltip.remove();
          }
          target.removeEventListener('mouseleave', removeTooltip);
        };
        
        target.addEventListener('mouseleave', removeTooltip);
      }
    };

    container.addEventListener('click', handleReferenceClick);
    container.addEventListener('mouseenter', handleReferenceMouseEnter, true);

    return () => {
      container.removeEventListener('click', handleReferenceClick);
      container.removeEventListener('mouseenter', handleReferenceMouseEnter, true);
      // 清理可能残留的 tooltip
      const tooltips = document.querySelectorAll('[data-tooltip-id="ref-tooltip"]');
      tooltips.forEach(t => t.remove());
    };
  }, [replacedNodes, t]);

  useEffect(() => {
    store.dispatch(setCurrentAnswer(replacedNodes));
  }, [replacedNodes]);


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

        {/* 引用总览按钮 - 只在有实际使用的引用时显示 */}
        {reference?.length > 0 && usedReferenceCount > 0 && (
          <div className='reference-overview-section'>
            <button
              className='reference-overview-btn'
              onClick={() => setShowReferenceOverview(true)}
            >
              <span className='reference-overview-icon'>📚</span>
              <span className='reference-overview-text'>
                查看来源 ({usedReferenceCount} 个来源)
              </span>
            </button>
          </div>
        )}

        {/* 引用总览抽屉 - 只在有实际使用的引用时显示 */}
        {reference?.length > 0 && usedReferenceCount > 0 && (
          <ReferenceOverviewDrawer
            isOpen={showReferenceOverview}
            setIsOpen={setShowReferenceOverview}
            usedReferences={usedReferences}
          />
        )}

      </div>
    </>
  );
};

export default MessageBox;
