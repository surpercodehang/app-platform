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
   * 渲染正文 + 引用 - 确保正文和引用在同一行，段落之间换行
   */
  const renderWithReferences = (rawContent: string) => {
    if (!rawContent) return null;

    let replacedStrs = rawContent.replace(/<\/ref><ref>/g, '_');
    const refKeyToNewNumber = new Map<string, number>();
    usedReferences.forEach(ref => {
      refKeyToNewNumber.set(ref.id, ref.number);
    });

    // 先按双换行符分割段落
    const paragraphs = replacedStrs.split(/\n\n+/);
    
    return (
      <>
        {paragraphs.map((paragraph, pIndex) => {
          const parts: React.ReactNode[] = [];
          let lastIndex = 0;
          const regex = /<ref>(.*?)<\/ref>/g;
          let match;
          
          while ((match = regex.exec(paragraph)) !== null) {
            // 处理引用前的文本
            const beforeText = paragraph.slice(lastIndex, match.index);
            if (beforeText) {
              // 处理markdown并移除所有块级标签，保持内联
              let processedHtml = markedProcess(beforeText)
                .replace(/<\/?p>/g, '')
                .replace(/<\/?div>/g, '')
                .replace(/\n/g, ' '); // 将单个换行符转换为空格
              
              parts.push(
                <span
                  key={`text-${pIndex}-${lastIndex}`}
                  className="inline-markdown"
                  dangerouslySetInnerHTML={{ __html: processedHtml }}
                />
              );
            }

            // 处理引用
            const keyContent = match[1];
            const keys = keyContent.split('_').filter(k => refKeyToNewNumber.has(k));
            const refNumbers = keys.map(k => refKeyToNewNumber.get(k)!).sort((a, b) => a - b);

            refNumbers.forEach(num => {
              const refData = usedReferences.find(r => r.number === num);
              const title = refData?.data?.metadata?.title || refData?.data?.source || '未知来源';
              const summary = refData?.data?.txt || refData?.data?.text || '无摘要';
              const url = refData?.data?.metadata?.url || refData?.data?.source;

              const tooltipContent = (
                <div>
                  <div style={{ fontWeight: 600 }}>{title}</div>
                  <div style={{ fontSize: '12px', color: '#888' }}>{summary}</div>
                </div>
              );

              parts.push(
                <Tooltip key={`ref-${pIndex}-${num}`} title={tooltipContent} placement="top">
                  <span
                    className="reference-circle"
                    onClick={() => {
                      if (isChatRunning()) {
                        Message({ type: 'warning', content: t('tryLater') });
                        return;
                      }
                      if (url && /^https?:\/\//.test(url)) {
                        window.open(url, '_blank');
                      } else {
                        Message({ type: 'info', content: '该引用没有可访问的链接' });
                      }
                    }}
                  >
                    {num}
                  </span>
                </Tooltip>
              );
            });

            lastIndex = regex.lastIndex;
          }

          // 处理段落剩余的文本
          const afterText = paragraph.slice(lastIndex);
          if (afterText) {
            let processedHtml = markedProcess(afterText)
              .replace(/<\/?p>/g, '')
              .replace(/<\/?div>/g, '')
              .replace(/\n/g, ' ');
            
            parts.push(
              <span
                key={`text-${pIndex}-end`}
                className="inline-markdown"
                dangerouslySetInnerHTML={{ __html: processedHtml }}
              />
            );
          }

          // 每个段落用div包裹，段落之间自动换行
          return (
            <div key={`paragraph-${pIndex}`} className="paragraph-container">
              {parts}
            </div>
          );
        })}
      </>
    );
  };


  // 设置接受消息显示内容
  const getMessageContent = () => {
    if (pictureList) {
      return <PictureList pictureList={pictureList}></PictureList>;
    } else {
      return (
        <div className='receive-info-html'>
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

      </div>
    </>
  );
};

export default MessageBox;
