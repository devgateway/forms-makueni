import { retrievePMCReports, updatePMCReports } from '../api/Api'
import {PMCReportStatus} from "./constants";

export const synchronize = async (userId, token, reports) => {

    const listReportsResponse = await retrievePMCReports(userId, token)

    // TODO also retrieve metadata here

    let savedReports = []

    if (reports.length > 0) {
        if (reports.some(report => report.status !== PMCReportStatus.SUBMITTED_PENDING)) {
            throw new Error(`Reports for sync must be in ${PMCReportStatus.SUBMITTED_PENDING} status`)
        }

        const reportsWithCorrectStatus = reports.map(report => ({
            ...report,
            status: PMCReportStatus.SUBMITTED
        }))

        const savedReportsResponse = await updatePMCReports(userId, token, reportsWithCorrectStatus)

        savedReports = savedReportsResponse.data.map((report, idx) => ({
            ...report,
            internalId: reports[idx].internalId
        }))
    }

    return {
        existingReports: listReportsResponse.data,
        savedReports: savedReports
    }
}
