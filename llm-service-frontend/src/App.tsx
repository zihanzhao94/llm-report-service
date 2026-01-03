import { useState, useEffect } from 'react'

import type { TaskStatus, ReportRequest, ReportResponse } from './types'
import { submitReport, getReport } from './api/reports'

function App() {
  const [inputText, setInputText] = useState('')
  const [taskId, setTaskId] = useState<string | null>(null)
  const [taskStatus, setTaskStatus] = useState<TaskStatus>('PENDING')
  const [report, setReport] = useState<ReportResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  // Handle form submission
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    // Prevent empty submission
    if (inputText.trim() === '') {
      return
    }
    
    // Prevent duplicate submission
    if (taskStatus === 'PROCESSING') {
      return
    }

    // Reset state
    setError(null)
    setReport(null)
    setTaskId(null)
    setTaskStatus('PENDING')

    try {
      const request: ReportRequest = {
        userInput: inputText,
      }
      
      const response = await submitReport(request)
      setTaskId(response.id)
      setTaskStatus(response.status)
      setReport(response)
      window.alert(`Submitted task ID: ${response.id}`)      
      // If status is PROCESSING immediately after submission, polling will start automatically
    } catch (err) {
      console.error('Error submitting report:', err)
      setError('Submission FAILED, please try again')
      setTaskStatus('PENDING')
    }
  }

  // Poll task status with useEffect
  useEffect(() => {
    // Poll until task completes or fails(only when PENDING and PROCESSING)
    if (!taskId || taskStatus === 'COMPLETED' || taskStatus === 'FAILED') {
      return
    }

    // Define async polling function
    const pollReport = async () => {
      try {
        const response = await getReport(taskId)
        setTaskStatus(response.status)
        setReport(response)
        
        // If task is COMPLETED or FAILED, polling will stop naturally (useEffect will re-run and return)
      } catch (err) {
        console.error('Error polling report:', err)
        setError('FAILED to fetch report status')
        setTaskStatus('FAILED')
      }
    }

    // Execute once immediately
    pollReport()

    // Poll every 2 seconds
    const intervalId = setInterval(pollReport, 2000)

    // Cleanup function: clear interval when component unmounts or dependencies change
    return () => {
      clearInterval(intervalId)
    }
  }, [taskId, taskStatus])

  // Reset form
  const handleReset = () => {
    setInputText('')
    setTaskId(null)
    setTaskStatus('PENDING')
    setReport(null)
    setError(null)
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto">
        {/* Title */}
        <h1 className="text-3xl font-bold text-gray-900 mb-8 text-center">
          LLM Report Generation
        </h1>

        {/* Input Form */}
        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="mb-4">
            <label htmlFor="input-text" className="block text-sm font-medium text-gray-700 mb-2">
              Enter Text Content
            </label>
            <textarea
              id="input-text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              rows={8}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Enter the text to analyze here..."
              disabled={taskStatus === 'PROCESSING'}
            />
          </div>
          
          <div className="flex gap-4">
            <button
              type="submit"
              disabled={!inputText.trim() || taskStatus === 'PROCESSING'}
              className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
            >
              {taskStatus === 'PROCESSING' ? 'PROCESSING...' : 'Submit'}
            </button>
            
            {(taskStatus === 'COMPLETED' || taskStatus === 'FAILED' || error) && (
              <button
                type="button"
                onClick={handleReset}
                className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
              >
                Reset
              </button>
            )}
          </div>
        </form>

        {/* Status Display Area */}
        {(taskId || taskStatus !== 'PENDING' || error) && (
          <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Task Status</h2>
            
            {error && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-md">
                <p className="text-red-800">{error}</p>
              </div>
            )}
            
            {taskId && (
              <div className="mb-2">
                <span className="text-sm text-gray-600">Task ID: </span>
                <span className="text-sm font-mono text-gray-900">{taskId}</span>
              </div>
            )}
            
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium text-gray-700">Status: </span>
              <span className={`text-sm font-semibold ${
                taskStatus === 'PENDING' ? 'text-gray-500' :
                taskStatus === 'PROCESSING' ? 'text-blue-600' :
                taskStatus === 'COMPLETED' ? 'text-green-600' :
                'text-red-600'
              }`}>
                {taskStatus === 'PENDING' && 'PENDING'}
                {taskStatus === 'PROCESSING' && 'PROCESSING...'}
                {taskStatus === 'COMPLETED' && 'COMPLETED'}
                {taskStatus === 'FAILED' && 'FAILED'}
              </span>
              {taskStatus === 'PROCESSING' && (
                <span className="inline-block w-2 h-2 bg-blue-600 rounded-full animate-pulse"></span>
              )}
            </div>
          </div>
        )}

        {/* Result Display Area */}
        {report && taskStatus === 'COMPLETED' && (() => {
          // Parse json reportResult from backend
          let parsedResult;
          try {
            parsedResult = JSON.parse(report.reportResult);
          } catch (e) {
            parsedResult = null;
          }

          return (
            <div className="bg-white rounded-lg shadow-md p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Report Result</h2>
              
              {parsedResult ? (
                <div className="space-y-6">
                  {/* Summary Section */}
                  <div>
                    <h3 className="text-sm font-semibold text-gray-700 mb-2">Summary</h3>
                    <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                      <p className="text-gray-900 leading-relaxed">{parsedResult.summary}</p>
                    </div>
                  </div>

                  {/* Key Points Section */}
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

                  {/* Confidence Score Section */}
                  <div>
                    <h3 className="text-sm font-semibold text-gray-700 mb-2">Confidence Score</h3>
                    <div className="flex items-center gap-3">
                      <div className="flex-1 bg-gray-200 rounded-full h-3 overflow-hidden">
                        <div 
                          className="bg-linear-to-r from-blue-500 to-blue-600 h-3 rounded-full transition-all duration-500"
                          style={{ width: `${(parsedResult.confidence_score || 0) * 100}%` }}
                        ></div>
                      </div>
                      <span className="text-sm font-medium text-gray-700 min-w-12 text-right">
                        {(parsedResult.confidence_score || 0).toFixed(2)}
                      </span>
                    </div>
                  </div>

                  {/* Timestamp */}
                  <div className="pt-4 border-t border-gray-200">
                    <p className="text-xs text-gray-500">
                      Created At: {new Date(report.createAt).toLocaleString('en-US')}
                    </p>
                  </div>
                </div>
              ) : (
                // show original content if parse failed
                <div className="p-4 bg-gray-50 rounded-lg border border-gray-200">
                  <pre className="text-sm text-gray-700 whitespace-pre-wrap font-mono">
                    {report.reportResult}
                  </pre>
                </div>
              )}
            </div>
          );
        })()}
      </div>
    </div>
  )
}

export default App
