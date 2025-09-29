/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, { useEffect, useState } from 'react';
import { markedProcess } from '../../utils/marked-process';
import { useTranslation } from 'react-i18next';
import ReferenceDrawer from './reference-drawer';
import ReferenceOverviewModal from './reference-overview-modal';
import ReferenceOverviewDrawer from './reference-overview-drawer';
import { Message } from '@/shared/utils/message';
import { isChatRunning } from '@/shared/utils/chat';
import { useAppSelector } from '@/store/hook';
import Feedbacks from './feedbacks';
import PictureList from './picture-list';
import ThinkBlock from './think-block';
import StepBlock from './step-block';
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
  const [isOpen, setIsOpen] = useState(false);
  const [referenceStr, setReferenceStr] = useState('');
  const [thinkContent, setThinkContent] = useState('');
  const [answerContent, setAnswerContent] = useState('');
  const [referenceIndex, setReferenceIndex] = useState('0');
  const [stepContent, setStepContent] = useState('');
  const [showStep, setShowStep] = useState(false);
  const [replacedText, setReplacedText] = useState<any>(null);
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
      refMatches.forEach(match => {
        const keyContent = match.replace(/<ref>|<\/ref>/g, '');
        const keys = keyContent.split('_');
        keys.forEach(key => {
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
    const usedRefKeysInOrder = [];
    const tempUsedKeys = new Set();

    // 从内容中提取使用的引用键（保持出现顺序）
    if (content) {
      const refMatches = content.match(/<ref>(.*?)<\/ref>/g) || [];
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

  // 在 MessageBox 组件中修改 regExpReplace 函数
  // 新的正则替换逻辑 - 按使用顺序重新编号
  const regExpReplace = (content: string, index: any) => {
    let strs = content;
    let replacedStrs = strs.replace(/<\/ref><ref>/g, '_');

    // 获取可用的引用键
    const referenceList = Array.isArray(reference) ? (reference[0] || {}) : (referenceList || {});
    const allRefKeys = Object.keys(referenceList);

    // 收集所有使用的引用键（按出现顺序）
    const usedRefKeysInOrder = [];
    const tempUsedKeys = new Set();

    // 第一次遍历：收集所有使用的引用键（保持出现顺序）
    replacedStrs.replace(/<ref>(.*?)<\/ref>/g, (match, key) => {
      const splitStr = key.split('_');
      splitStr.forEach(refKey => {
        if (allRefKeys.includes(refKey) && !tempUsedKeys.has(refKey)) {
          tempUsedKeys.add(refKey);
          usedRefKeysInOrder.push(refKey);
        }
      });
      return '';
    });

    // 建立使用引用键到新编号的映射（从1开始重新编号）
    const refKeyToNewNumber = new Map();
    usedRefKeysInOrder.forEach((key, index) => {
      refKeyToNewNumber.set(key, index + 1);
    });

    // 第二次遍历：替换为重新编号的圆形数字
    const replacedStr = replacedStrs.replace(/<ref>(.*?)<\/ref>/g, (match, key) => {
      const splitStr = key.split('_');
      const validRefs = splitStr.filter((item: any) => allRefKeys.includes(item));

      if (validRefs.length > 0) {
        // 获取对应的新编号并排序
        const refNumbers = validRefs.map(refKey => refKeyToNewNumber.get(refKey)).sort((a, b) => a - b);

        // 为每个数字创建独立的圆形元素
        const circleElements = refNumbers.map(num =>
          `<span data-reference="${index}-${num}" class="reference-circle">${num}</span>`
        ).join('');

        return circleElements;
      }
      return '';
    });

    return replacedStr;
  };

  // 拼接content与reference
  const replaceInfo = (content, type = '') => {
    let metaContent = [content];
    let mataStr = metaContent.map((item: any, index: any) => {
      return regExpReplace(item, index);
    });
    type ? setThinkContent(mataStr.join('')) : setReplacedText(mataStr.join(''));
  };

  // 点击引用数字的回调
  const onClickReference = (e: any) => {
    if (e.target.classList.contains('reference-circle')) {
      if (isChatRunning()) {
        Message({ type: 'warning', content: t('tryLater') });
        return;
      }
      e.target.classList.add('reference-circle-active');
      let strs = e.target.dataset.reference;
      let referenceIndex = strs.split('-')[0];
      let referenceNumber = strs.split('-')[1]; // 单个数字，如 "3"

      if (strs) {
        setReferenceStr(referenceNumber);
        setReferenceIndex(referenceIndex);
        setIsOpen(true);
      }
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
          onClick={(e) => onClickReference(e)}
          dangerouslySetInnerHTML={{ __html: markedProcess(replacedText) }}
        ></div>
      );
    }
  };

  // a标签点击（打开新窗口跳转）
  const recieveClick = (event) => {
    if (event.target && event.target.nodeName.toLowerCase() === 'a') {
      event.preventDefault();
      window.open(event.target.href, '_blank');
    }
  }

  // 智能体调度工具内容处理
  const getAgentOutput = (str: string) => {
    let lastOpenTag:any = null;
    let hasStepContent = false;
    let tagMap:any = {
      reasoning: t('thinking'),
      step: {
        name: t('steps'),
        index: 1
      },
      tool: t('toolResult')
    }
    tagMap.step.index = 1;
    let output = str.replace(/<(\/?)(reasoning|step|tool|final)>/g,  (match, isClose, tag) => {
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
  const setClosureLabel = (str:string) => {
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
      container && container.removeEventListener('click', recieveClick);
    }
  }, []);

  useEffect(() => {
    store.dispatch(setCurrentAnswer(replacedText));
  }, [replacedText]);

  // 在 MessageBox 组件的返回部分修改
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

        {/* 单个引用抽屉 */}
        {reference?.length > 0 && (
          <ReferenceDrawer
            isOpen={isOpen}
            setIsOpen={setIsOpen}
            reference={reference}
            referenceStr={referenceStr}
            referenceIndex={referenceIndex}
            content={content}
          />
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
