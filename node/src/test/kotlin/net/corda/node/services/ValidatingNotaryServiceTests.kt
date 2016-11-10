package net.corda.node.services

import com.google.common.util.concurrent.ListenableFuture
import net.corda.core.contracts.*
import net.corda.core.crypto.DigitalSignature
import net.corda.core.node.services.ServiceInfo
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.DUMMY_NOTARY
import net.corda.core.utilities.DUMMY_NOTARY_KEY
import net.corda.node.internal.AbstractNode
import net.corda.node.services.network.NetworkMapService
import net.corda.node.services.transactions.ValidatingNotaryService
import net.corda.protocols.NotaryError
import net.corda.protocols.NotaryException
import net.corda.protocols.NotaryProtocol
import net.corda.testing.MEGA_CORP_KEY
import net.corda.testing.MINI_CORP_KEY
import net.corda.testing.node.MockNetwork
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ValidatingNotaryServiceTests {
    lateinit var net: MockNetwork
    lateinit var notaryNode: MockNetwork.MockNode
    lateinit var clientNode: MockNetwork.MockNode

    @Before fun setup() {
        net = MockNetwork()
        notaryNode = net.createNode(
                legalName = DUMMY_NOTARY.name,
                keyPair = DUMMY_NOTARY_KEY,
                advertisedServices = *arrayOf(ServiceInfo(NetworkMapService.type), ServiceInfo(ValidatingNotaryService.type))
        )
        clientNode = net.createNode(networkMapAddress = notaryNode.info.address, keyPair = MINI_CORP_KEY)
        net.runNetwork() // Clear network map registration messages
    }

    @Test fun `should report error for invalid transaction dependency`() {
        val stx = run {
            val inputState = issueInvalidState(clientNode, notaryNode.info.notaryIdentity)
            val tx = TransactionType.General.Builder(notaryNode.info.notaryIdentity).withItems(inputState)
            tx.signWith(clientNode.keyPair!!)
            tx.toSignedTransaction(false)
        }

        val future = runClient(stx)

        val ex = assertFailsWith(ExecutionException::class) { future.get() }
        val notaryError = (ex.cause as NotaryException).error
        assertThat(notaryError).isInstanceOf(NotaryError.TransactionInvalid::class.java)
    }

    @Test fun `should report error for missing signatures`() {
        val expectedMissingKey = MEGA_CORP_KEY.public
        val stx = run {
            val inputState = issueState(clientNode)

            val command = Command(DummyContract.Commands.Move(), expectedMissingKey)
            val tx = TransactionType.General.Builder(notaryNode.info.notaryIdentity).withItems(inputState, command)
            tx.signWith(clientNode.keyPair!!)
            tx.toSignedTransaction(false)
        }

        val ex = assertFailsWith(ExecutionException::class) {
            val future = runClient(stx)
            future.get()
        }
        val notaryError = (ex.cause as NotaryException).error
        assertThat(notaryError).isInstanceOf(NotaryError.SignaturesMissing::class.java)

        val missingKeys = (notaryError as NotaryError.SignaturesMissing).missingSigners
        assertEquals(setOf(expectedMissingKey), missingKeys)
    }

    private fun runClient(stx: SignedTransaction): ListenableFuture<DigitalSignature.LegallyIdentifiable> {
        val protocol = NotaryProtocol.Client(stx)
        val future = clientNode.services.startProtocol(protocol)
        net.runNetwork()
        return future
    }

    fun issueState(node: AbstractNode): StateAndRef<*> {
        val tx = DummyContract.generateInitial(node.info.legalIdentity.ref(0), Random().nextInt(), notaryNode.info.notaryIdentity)
        val nodeKey = node.services.legalIdentityKey
        tx.signWith(nodeKey)
        val notaryKeyPair = notaryNode.services.notaryIdentityKey
        tx.signWith(notaryKeyPair)
        val stx = tx.toSignedTransaction()
        node.services.recordTransactions(listOf(stx))
        return StateAndRef(tx.outputStates().first(), StateRef(stx.id, 0))
    }
}