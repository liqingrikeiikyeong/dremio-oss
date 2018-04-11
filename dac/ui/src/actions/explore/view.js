/*
 * Copyright (C) 2017-2018 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
export const SET_CURRENT_SQL = 'SET_CURRENT_SQL';
export const SET_QUERY_CONTEXT = 'SET_QUERY_CONTEXT';
export const RESET_NEW_QUERY = 'RESET_NEW_QUERY';

export function setCurrentSql({ sql }) {
  return { type: SET_CURRENT_SQL, sql };
}

export function setQueryContext({ context }) {
  return { type: SET_QUERY_CONTEXT, context };
}

export function resetNewQuery(viewId) {
  return { type: RESET_NEW_QUERY, viewId };
}

export const UPDATE_HISTORY_STATE = 'UPDATE_HISTORY_STATE';
export const updateHistoryState = (history, version) => ({ type: UPDATE_HISTORY_STATE, history, version });

export const UPDATE_COLUMNS = 'UPDATE_COLUMNS';
export const updateTableColumns = ({ version, columns }) => ({ type: UPDATE_COLUMNS, version, columns });