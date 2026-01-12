import { useState, useEffect } from 'react'

import type { ReportRequest, ReportResponse } from './types'
import { submitReport, getAllReports } from './api/reports'

function App() {
  const [inputText, setInputText] = useState('')
  const [expandedReportId, setExpandedReportId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [allReports, setAllReports] = useState<ReportResponse[] | null>(null)

  // Handle form submission
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (inputText.trim() === '') {
      return
    }

    // Reset state
    setError(null)

    try {
      const request: ReportRequest = {
        userInput: inputText,
      }

      const response = await submitReport(request)
      window.alert(`Submitted task ID: ${response.id}`)
      setInputText('')

      // Refresh list immediately
      const updatedReports = await getAllReports()
      setAllReports(updatedReports)

      // Auto-expand the new task
      setExpandedReportId(response.id)

    } catch (err) {
      console.error('Error submitting report:', err)
      setError('Submission FAILED, please try again')
    }
  }

  // Poll ALL reports if any is processing
  useEffect(() => {
    // Check if we need to poll (if any report is processing)
    const hasProcessingTasks = allReports?.some(r => r.status === 'PROCESSING' || r.status === 'PENDING');

    if (!hasProcessingTasks) {
      return
    }

    const pollAllReports = async () => {
      try {
        const response = await getAllReports()
        setAllReports(response)
      } catch (err) {
        console.error('Error polling reports:', err)
      }
    }

    const intervalId = setInterval(pollAllReports, 2000)
    return () => clearInterval(intervalId)
  }, [allReports])

  // Fetch initial data
  useEffect(() => {
    getAllReports().then(setAllReports).catch(console.error)
  }, [])

  // Toggle report view
  const toggleReport = (id: string) => {
    if (expandedReportId === id) {
      setExpandedReportId(null)
    } else {
      setExpandedReportId(id)
    }
  }

  // Helper component for rendering report content
  const ReportDetail = ({ reportResult, createAt }: { reportResult: string, createAt: string }) => {
    let parsedResult;
    try {
      parsedResult = JSON.parse(reportResult);
    } catch {
      parsedResult = null;
    }

    if (!parsedResult) {
      return (
        <div className="p-4 bg-gray-50 rounded-lg border border-gray-200 mt-4">
          <pre className="text-sm text-gray-700 whitespace-pre-wrap font-mono">{reportResult}</pre>
        </div>
      )
    }

    return (
      <div className="mt-4 space-y-6 animate-fadeIn">
        <div>
          <h3 className="text-sm font-semibold text-gray-700 mb-2">Summary</h3>
          <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
            <p className="text-gray-900 leading-relaxed">{parsedResult.summary}</p>
          </div>
        </div>

        <div>
          <h3 className="text-sm font-semibold text-gray-700 mb-2">Key Points</h3>
          <ul className="space-y-2 p-4 bg-gray-50 rounded-lg border border-gray-200">
            {parsedResult.key_points?.map((point: string, index: number) => (
              <li key={index} className="flex items-start gap-2">
                <span className="text-blue-600 mt-1">•</span>
                <span className="text-gray-900 flex-1">{point}</span>
              </li>
            ))}
          </ul>
        </div>

        <div>
          <h3 className="text-sm font-semibold text-gray-700 mb-2">Confidence Score</h3>
          <div className="flex items-center gap-3">
            <div className="flex-1 bg-gray-200 rounded-full h-3 overflow-hidden">
              <div
                className="bg-gradient-to-r from-blue-500 to-blue-600 h-3 rounded-full transition-all duration-500"
                style={{ width: `${(parsedResult.confidence_score || 0) * 100}%` }}
              ></div>
            </div>
            <span className="text-sm font-medium text-gray-700 min-w-[3rem] text-right">
              {(parsedResult.confidence_score || 0).toFixed(2)}
            </span>
          </div>
        </div>

        <div className="pt-4 border-t border-gray-200">
          <p className="text-xs text-gray-500">
            Created At: {new Date(createAt).toLocaleString('en-US')}
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-8 text-center">LLM Report Generation</h1>

        {/* Input Form */}
        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-md p-6 mb-8">
          <div className="mb-4">
            <label htmlFor="input-text" className="block text-sm font-medium text-gray-700 mb-2">
              Enter Text Content
            </label>
            <textarea
              id="input-text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              rows={4}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Enter the text to analyze here..."
            />
          </div>

          <div className="flex gap-4">
            <button
              type="submit"
              disabled={!inputText.trim()}
              className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
            >
              Submit New Task
            </button>
          </div>
        </form>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-md">
            <p className="text-red-800">{error}</p>
          </div>
        )}

        {/* Reports List */}
        {allReports && allReports.length > 0 && (
          <div className="bg-white rounded-lg shadow-md overflow-hidden">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-xl font-semibold text-gray-900">All Reports</h2>
            </div>
            <ul className="divide-y divide-gray-200">
              {[...allReports]
                .sort((a, b) => new Date(b.createAt).getTime() - new Date(a.createAt).getTime())
                .map((report: ReportResponse) => (
                  <li key={report.id} className="hover:bg-gray-50 transition-colors">
                    <div
                      className="p-4 cursor-pointer"
                      onClick={() => toggleReport(report.id)}
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4">
                          <span className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold
                                    ${report.status === 'COMPLETED' ? 'bg-green-100 text-green-700' :
                              report.status === 'PROCESSING' || report.status === 'PENDING' ? 'bg-blue-100 text-blue-700' :
                                'bg-red-100 text-red-700'}`}>
                            {report.id}
                          </span>
                          <div className="flex flex-col">
                            <span className="text-sm font-medium text-gray-900">
                              Status: <span className={
                                report.status === 'COMPLETED' ? 'text-green-600' :
                                  report.status === 'FAILED' ? 'text-red-600' : 'text-blue-600'
                              }>{report.status}</span>
                            </span>
                            <span className="text-xs text-gray-500">
                              {new Date(report.createAt).toLocaleString()}
                            </span>
                          </div>
                        </div>

                        <div className="flex items-center gap-2">
                          {report.status === 'PROCESSING' && (
                            <span className="inline-block w-2 h-2 bg-blue-600 rounded-full animate-pulse mr-2"></span>
                          )}
                          <svg
                            className={`w-5 h-5 text-gray-400 transform transition-transform ${expandedReportId === report.id ? 'rotate-180' : ''}`}
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                          >
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                          </svg>
                        </div>
                      </div>

                      {/* Expanded Content */}
                      {expandedReportId === report.id && report.status === 'COMPLETED' && report.reportResult && (
                        <div className="mt-4 border-t border-gray-100 pt-4 cursor-auto" onClick={(e) => e.stopPropagation()}>
                          <ReportDetail reportResult={report.reportResult} createAt={report.createAt} />
                        </div>
                      )}

                      {expandedReportId === report.id && report.status === 'FAILED' && (
                        <div className="mt-4 border-t border-gray-100 pt-4">
                          <p className="text-sm text-red-600">Task failed. Please check the backend logs or try again.</p>
                        </div>
                      )}
                    </div>
                  </li>
                ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  )
}

export default App
